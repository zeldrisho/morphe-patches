package com.zeldrisho.patches.zalo

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.zeldrisho.patches.zalo.shared.Constants.COMPATIBILITY_ZALO

private const val MICROG_ACCOUNT_TYPE = "app.revanced"
private const val MICROG_PACKAGE = "app.revanced.android.gms"

private val accountTypeClasses = setOf(
    "Lcom/zing/zalo/ui/backuprestore/drive/SyncGoogleAccountBaseView;",
    "Lcom/zing/zalo/ui/backuprestore/drive/ManageGoogleAccountView;",
    "Lcom/zing/zalo/ui/backuprestore/drive/SyncGoogleAccountMediaRestoreView;",
    "Lul/g;",
    "Ln71/d0;",
)

/**
 * Redirects Zalo's Google Drive account and token plumbing to microG-RE.
 *
 * Zalo 26.08.01 uses the original Google account type when selecting an
 * account and binds its token request to the original Play Services package.
 * The replacements are restricted to the five known Drive classes and the
 * pinned `o9/a` binding helper; unrelated Google SDK strings are untouched.
 *
 * WARNING: this is an opt-in authentication/provider patch. It requires
 * microG-RE configured with account type `app.revanced` and package
 * `app.revanced.android.gms`; other Google authentication flows are not
 * changed or guaranteed to work.
 */
@Suppress("unused")
val zaloMicroGSupportPatch = bytecodePatch(
    name = "Zalo: microG Drive support",
    description = "Redirects Zalo Google Drive account selection and token binding to " +
        "microG-RE (app.revanced / app.revanced.android.gms). WARNING: requires the " +
        "matching microG-RE configuration and only covers Zalo's Drive restore flow.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_ZALO)

    execute {
        var bindingReplacements = 0
        var accountTypeReplacements = 0

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
                implementation.instructions.forEachIndexed { index, instruction ->
                    if (instruction.opcode != Opcode.CONST_STRING) return@forEachIndexed
                    val reference = (instruction as? ReferenceInstruction)?.reference as? StringReference
                        ?: return@forEachIndexed
                    if (reference.string != replacement.first) return@forEachIndexed

                    val register = (instruction as OneRegisterInstruction).registerA
                    mutableMethod.replaceInstruction(index, "const-string v$register, \"${replacement.second}\"")
                    if (classDef.type == "Lo9/a;") bindingReplacements++ else accountTypeReplacements++
                }

                if (classDef.type == "Lcom/zing/zalo/ui/backuprestore/drive/SyncGoogleAccountBaseView;" &&
                    method.name == "x6" && method.parameterTypes == listOf("Ljava/lang/String;")
                ) {
                    mutableMethod.addInstructionsWithLabels(
                        0,
                        """
                            invoke-static { p1 }, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z
                            move-result v0
                            if-eqz v0, :lookup_existing_microg_account
                            goto :continue_original_account_flow
                            :lookup_existing_microg_account
                            invoke-virtual { p0 }, Lcom/zing/zalo/zview/a0;->getContext()Landroid/content/Context;
                            move-result-object v0
                            invoke-static { v0 }, Landroid/accounts/AccountManager;->get(Landroid/content/Context;)Landroid/accounts/AccountManager;
                            move-result-object v1
                            const-string v2, "$MICROG_ACCOUNT_TYPE"
                            const-string v3, "$MICROG_PACKAGE"
                            invoke-static {}, Landroid/os/Process;->myUserHandle()Landroid/os/UserHandle;
                            move-result-object v4
                            invoke-virtual { v1, v2, v3, v4 }, Landroid/accounts/AccountManager;->getAccountsByTypeForPackage(Ljava/lang/String;Ljava/lang/String;Landroid/os/UserHandle;)[Landroid/accounts/Account;
                            move-result-object v1
                            const/4 v2, 0x0
                            :scan_existing_microg_accounts
                            array-length v3, v1
                            if-ge v2, v3, :continue_original_account_flow
                            aget-object v3, v1, v2
                            iget-object v4, v3, Landroid/accounts/Account;->type:Ljava/lang/String;
                            const-string v5, "$MICROG_ACCOUNT_TYPE"
                            invoke-virtual { v5, v4 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                            move-result v4
                            if-eqz v4, :next_microg_account
                            iget-object v1, v3, Landroid/accounts/Account;->name:Ljava/lang/String;
                            invoke-virtual { p0, v1 }, Lcom/zing/zalo/ui/backuprestore/drive/SyncGoogleAccountBaseView;->A6(Ljava/lang/String;)V
                            return-void
                            :next_microg_account
                            add-int/lit8 v2, v2, 0x1
                            goto :scan_existing_microg_accounts
                            :continue_original_account_flow
                            nop
                        """.trimIndent(),
                    )
                }
            }
        }

        check(bindingReplacements == 1) {
            "Zalo microG support: expected one o9/a service-binding replacement, " +
                "found $bindingReplacements"
        }
        check(accountTypeReplacements > 0) {
            "Zalo microG support: no Drive account-type literals were found"
        }
    }
}
