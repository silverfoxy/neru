// neru-android-bridge is the C ABI exported to the Android JNI shim.
package main

/*
#include <stdlib.h>
*/
import "C"

import (
	"encoding/json"
	"unsafe"

	"github.com/y3owk1n/neru/internal/androidbridge"
)

// NeruLoadConfigJSON parses a config file with Neru's production Go policy and
// returns a JSON ConfigSnapshot. The caller must free the result with
// NeruFreeString.
//
//export NeruLoadConfigJSON
func NeruLoadConfigJSON(path *C.char) *C.char {
	snapshot := androidbridge.LoadConfigFile(C.GoString(path))
	encoded, err := json.Marshal(snapshot)
	if err != nil {
		return C.CString(`{"diagnostics":[{"message":"failed to encode config snapshot"}]}`)
	}
	return C.CString(string(encoded))
}

// NeruFreeString frees a string allocated by NeruLoadConfigJSON.
//
//export NeruFreeString
func NeruFreeString(value *C.char) {
	C.free(unsafe.Pointer(value))
}

func main() {}
