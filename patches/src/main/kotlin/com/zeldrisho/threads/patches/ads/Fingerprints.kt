package com.zeldrisho.threads.patches.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Verified in classes.dex of Threads 434.0.0.41.74 (510406926).
 * A0F constructs this named save callback after assembling the incoming feed.
 * Do not match the R8 method name or its obfuscated object parameter types.
 * The List remains p5; all parameters occupy one register.
 */
internal object FeedMergeMethod : Fingerprint(
    definingClass = "Lcom/instagram/barcelona/feed/data/cache/BarcelonaFeedCache;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "L", "Ljava/lang/Integer;", "Ljava/lang/String;", "Ljava/lang/String;",
        "Ljava/util/List;", "L", "Lkotlin/jvm/functions/Function3;", "Z",
    ),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_DIRECT_RANGE,
            definingClass = "Lcom/instagram/barcelona/feed/data/cache/" +
                "BarcelonaFeedCache\$addAndSaveItemsFromFeedFetchSuccess\$2\$1;",
            name = "<init>",
            returnType = "V",
        ),
    ),
)
