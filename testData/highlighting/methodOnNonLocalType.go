package demo2031

import (
	. "fmt"
	"time"
)

type <weak_warning descr="'Duration' should have a comment or not be exported">Duration</weak_warning> time.Duration

func (_ *string) <error descr="Method defined on non-local type">Demo</error>() {

}

func (_ int) <error descr="Method defined on non-local type">Demo</error>() {

}

func (_ ScanState) <error descr="Method defined on non-local type">Demo</error>() {

}

func (_ *Duration) <weak_warning descr="'UnmarshalText' should have a comment or not be exported">UnmarshalText</weak_warning>(data []byte) (err error) {
	_ = data
	return nil
}