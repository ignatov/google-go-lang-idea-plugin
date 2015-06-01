package assignmentCount

type DemoFunc func() int

type Demo struct {
	DemoFunc
}

func _(dem Demo) (ctx int) {
	ctx = dem.DemoFunc()
	return
}
