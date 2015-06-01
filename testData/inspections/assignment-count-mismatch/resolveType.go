package docker3

type myFuncType func() (string, error)

func _(myFunc myFuncType) (p1 string, err error) {
	p1, err = myFunc()
	return
}
