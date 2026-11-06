# Temporary: lists the stack words of a stopped lldb process that point into code with symbols.
import lldb

target = lldb.debugger.GetSelectedTarget()
proc = target.GetProcess()
thread = proc.GetSelectedThread()
rsp = thread.GetFrameAtIndex(0).FindRegister("rsp").GetValueAsUnsigned()
err = lldb.SBError()
for i in range(400):
    word = proc.ReadPointerFromMemory(rsp + 8 * i, err)
    if not err.Success():
        break
    addr = target.ResolveLoadAddress(word)
    module = addr.GetModule()
    if not module.IsValid() or "shim" not in module.GetFileSpec().GetFilename():
        continue
    sym = addr.GetSymbol()
    line = addr.GetLineEntry()
    print("stack[%d] %s %s %s:%d" % (i, hex(word), sym.GetName(), line.GetFileSpec().GetFilename(), line.GetLine()))
