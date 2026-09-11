package com.zeldrisho.patches.zalo

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.zeldrisho.patches.shared.bytecode.clearBody
import com.zeldrisho.patches.shared.bytecode.ensureRegisters
import com.zeldrisho.patches.zalo.shared.Constants.COMPATIBILITY_ZALO

private const val MICROG_ACCOUNT_TYPE = "app.revanced"
private const val MICROG_PACKAGE = "app.revanced.android.gms"
private const val ACCOUNT_PICKER_REQUEST_CODE = 0x3eb
private const val ALLOWABLE_ACCOUNT_TYPE_COUNT = 1
private const val ACCOUNT_PICKER_REGISTER_COUNT = 8

private val accountTypeClasses = setOf(
    "Lcom/zing/zalo/ui/backuprestore/drive/SyncGoogleAccountBaseView;",
    "Lcom/zing/zalo/ui/backuprestore/drive/ManageGoogleAccountView;",
    "Lcom/zing/zalo/ui/backuprestore/drive/SyncGoogleAccountMediaRestoreView;",
    "Lul/g;",
    "Ln71/d0;",
)

/** Replace account discovery/add-account with the system picker. */
private fun replaceWithAccountPicker(method: MutableMethod) {
    method.ensureRegisters(ACCOUNT_PICKER_REGISTER_COUNT)
    method.clearBody()
    method.addInstructionsWithLabels(
        0,
        """
            const/4 v0, $ALLOWABLE_ACCOUNT_TYPE_COUNT
            new-array v3, v0, [Ljava/lang/String;
            const/4 v1, 0x0
            const/4 v2, 0x0
            const-string v4, "$MICROG_ACCOUNT_TYPE"
            aput-object v4, v3, v1
            const/4 v4, 0x0
            const/4 v5, 0x0
            const/4 v6, 0x0
            const/4 v7, 0x0
            invoke-static/range { v1 .. v7 }, Landroid/accounts/AccountManager;->newChooseAccountIntent(Landroid/accounts/Account;Ljava/util/List;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Landroid/os/Bundle;)Landroid/content/Intent;
            move-result-object v0
            iget-object v2, p0, Lcom/zing/zalo/ui/zviews/BaseZaloView;->U0:Lcom/zing/zalo/ui/zviews/BaseZaloView;
            invoke-virtual { v2 }, Lcom/zing/zalo/zview/a0;->u4()Landroid/content/Context;
            move-result-object v2
            check-cast v2, Landroid/app/Activity;
            const/16 v1, $ACCOUNT_PICKER_REQUEST_CODE
            invoke-virtual { v2, v0, v1 }, Landroid/app/Activity;->startActivityForResult(Landroid/content/Intent;I)V
            return-void
        """.trimIndent(),
    )
}

/**
 * Redirects Zalo's Google Drive account and token plumbing to microG-RE.
 * Account selection is delegated to AccountManager so Android grants Zalo
 * visibility to the selected app.revanced account.
 */
@Suppress("unused")
val zaloMicroGSupportPatch = bytecodePatch(
    name = "microG Drive support",
    description = "Redirects Zalo Google Drive account selection and token binding to " +
        "microG-RE (app.revanced / app.revanced.android.gms). WARNING: requires the " +
        "matching microG-RE configuration and only covers Zalo's Drive restore flow.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_ZALO)

    execute {
        var bindingReplacements = 0
        var accountTypeReplacements = 0
        var accountPickerReplacements = 0

        classDefForEach { classDef ->
            val replacement = when (classDef.type) {
                "Lo9/a;" -> "com.google.android.gms" to MICROG_PACKAGE
                in accountTypeClasses -> "com.google" to MICROG_ACCOUNT_TYPE
                else -> return@classDefForEach
            }

            val mutableClass = mutableClassDefBy(classDef)
            classDef.methods.forEach { method ->
                val implementation = method.implementation ?: return@forEach
                val mutableMethod = mutableClass.methods.first { candidate ->
                    candidate.name == method.name &&
                        candidate.parameterTypes == method.parameterTypes &&
                        candidate.returnType == method.returnType
                }

                val isAccountPickerMethod =
                    (
                        classDef.type == "Lcom/zing/zalo/ui/backuprestore/drive/SyncGoogleAccountBaseView;" &&
                            method.name == "x6" && method.parameterTypes == listOf("Ljava/lang/String;")
                        ) ||
                        (
                            classDef.type == "Lcom/zing/zalo/ui/backuprestore/drive/ManageGoogleAccountView;" &&
                                method.name == "I6" && method.parameterTypes == listOf("Ljava/lang/String;")
                            )
                if (isAccountPickerMethod) {
                    replaceWithAccountPicker(mutableMethod)
                    accountPickerReplacements++
                    return@forEach
                }

                implementation.instructions.forEachIndexed { index, instruction ->
                    if (instruction.opcode != Opcode.CONST_STRING) return@forEachIndexed
                    val reference = (instruction as? ReferenceInstruction)?.reference as? StringReference
                        ?: return@forEachIndexed
                    val register = (instruction as OneRegisterInstruction).registerA
                    if (classDef.type == "Lcom/zing/zalo/ui/backuprestore/drive/SyncGoogleAccountBaseView;" &&
                        method.name == "onActivityResult" && reference.string == "authAccount"
                    ) {
                        mutableMethod.replaceInstruction(
                            index,
                            "sget-object v$register, Landroid/accounts/AccountManager;->KEY_ACCOUNT_NAME:Ljava/lang/String;",
                        )
                    }
                    val isAccountTypeLiteral = reference.string == "com.google" && classDef.type == "Lo9/a;"
                    if (reference.string != replacement.first && !isAccountTypeLiteral) return@forEachIndexed
                    val replacementValue = if (isAccountTypeLiteral) MICROG_ACCOUNT_TYPE else replacement.second
                    mutableMethod.replaceInstruction(index, "const-string v$register, \"$replacementValue\"")
                    if (classDef.type == "Lo9/a;" && !isAccountTypeLiteral) {
                        bindingReplacements++
                    } else {
                        accountTypeReplacements++
                    }
                }
            }
        }

        check(bindingReplacements == 1) {
            "Zalo microG support: expected one o9/a service-binding replacement, found $bindingReplacements"
        }
        check(accountTypeReplacements > 0) {
            "Zalo microG support: no Drive account-type literals were found"
        }
        check(accountPickerReplacements == 2) {
            "Zalo microG support: expected two account-picker replacements, found $accountPickerReplacements"
        }
    }
}
