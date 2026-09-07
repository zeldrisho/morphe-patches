package com.zeldrisho.threads.patches.ads

/**
 * Pure, unit-testable register math for [hideAdsPatch].
 *
 * A0F(this, LX/9aR, Integer, String, String, List, LX/2uI, Function3, Z):
 * 9 params including `this`; the feed list is param index 5 (p5).
 * Dalvik param registers sit at the top of the frame, so
 * `listReg = registerCount - (paramsIncludingThis) + listParamIndex`.
 *
 * Injected snippets only accept registers v0-v15, hence the
 * `move-object` vs `move-object/from16` split.
 */
fun feedListRegister(registerCount: Int, paramsIncludingThis: Int = 9, listParamIndex: Int = 5): Int =
    registerCount - paramsIncludingThis + listParamIndex

fun feedListLoadMove(listReg: Int): String =
    if (listReg <= 15) "move-object v0, v$listReg" else "move-object/from16 v0, v$listReg"

fun feedListStoreMove(listReg: Int): String =
    if (listReg <= 15) "move-object v$listReg, v0" else "move-object/from16 v$listReg, v0"
