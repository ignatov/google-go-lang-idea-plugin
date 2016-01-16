<weak_warning descr="Package comment should be of the form 'Package commentstart ...'">// demo</weak_warning>
package commentstart

type (
	<weak_warning descr="Comment should start with 'Hello'">// help</weak_warning>
	Hello struct{}

	<weak_warning descr="Comment should start with 'Helloi'">// help</weak_warning>
	Helloi interface{}
)

<weak_warning descr="Comment should start with 'Helllo'">// help</weak_warning>
type Helllo struct{}

<weak_warning descr="Comment should start with 'Hellloi'">// help</weak_warning>
type Hellloi interface{}

const (
	<weak_warning descr="Comment should start with 'Helloc'">// help</weak_warning>
	Helloc = 3
)

<weak_warning descr="Comment should start with 'Hellloc'">// help</weak_warning>
const Hellloc, hellloc2 = 1, 2

var (
	<weak_warning descr="Comment should start with 'Hello1'"><weak_warning descr="Comment should start with 'Hellow1'">// help</weak_warning></weak_warning>
	Hello1, Hellow1 int
)

<weak_warning descr="Comment should start with 'Hello2'">// help</weak_warning>
var Hello2 int

<weak_warning descr="Comment should start with 'Hellllo'">// Helllo</weak_warning>
func (a Helllo) Hellllo() {
	_ = Hello1
	_ = Hellow1
	_ = Hello2
	_ = Helloc
	_ = Hellloc
	_ = hellloc2
}

// Demo does things  -> correct
func Demo() {
	Demo2()
}

<weak_warning descr="Comment should start with 'Demo2'">// Demo does other things -> incorrect</weak_warning>
func Demo2() {
	Demo()
	Demo3()
	Demo4()
	Demo5()
	Demo6()
	Demo7()
	Demo8()
	Demo9()
}

<weak_warning descr="Comment should be meaningful or it should be removed">// Demo3</weak_warning>
func Demo3() {}

// A Demo4 does things  -> correct
func Demo4() {}

// An Demo5 does things  -> correct
func Demo5() {}

// The Demo6 does things  -> correct
func Demo6() {}

// Demo7 does things  -> correct
//
// Deprecated: use other thing
func Demo7() {}

// Demo8 does things  -> incorrect
//
// Deprecated: use other thing

func <weak_warning descr="'Demo8' should have a comment or not be exported">Demo8</weak_warning>() {}

// Demo does things  -> correct
//
// Deprecated: use other thing

// Demo9 demo
func Demo9() {}

var (
	<weak_warning descr="'A' should have a comment or not be exported">A</weak_warning> int
	<weak_warning descr="Comment should start with 'C'">// demo comment</weak_warning>
	C int
)

const (
	<weak_warning descr="'DemoDemo' should have a comment or not be exported">DemoDemo</weak_warning> = 0

	<weak_warning descr="'DemoEmo' should have a comment or not be exported">DemoEmo</weak_warning> = 1
)

// This should be correct
const (
	NewConst = 1
	NewConst2 = 1
)

type (
	<weak_warning descr="'Deeeeo' should have a comment or not be exported">Deeeeo</weak_warning> struct {
	}
	<weak_warning descr="Comment should start with 'Ehlo'">// demo stuff</weak_warning>
	Ehlo struct{
	}
)

<weak_warning descr="Comment should start with 'Gigel'">// digi</weak_warning>
func Gigel() {
	// demo
	B := "Hello"
	_, _, _, _, _ = DemoDemo, DemoEmo, A, B, C
}

func <weak_warning descr="'Gigel2' should have a comment or not be exported">Gigel2</weak_warning>() {
	_ , _ = NewConst, NewConst2
}

func (d Deeeeo) <weak_warning descr="'Gigel' should have a comment or not be exported">Gigel</weak_warning>() {
	Gigel()
	Gigel2()
}
