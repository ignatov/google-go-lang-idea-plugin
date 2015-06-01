package assignmentCount

type demo3 func() int
type demo2 []demo3

type demo struct {
	arr *demo2
}

func (opt *demo) _() (f int) {
	if myFunc := opt.arr["aaa"]; myFunc != nil {
		f += myFunc()
	}
	return
}
