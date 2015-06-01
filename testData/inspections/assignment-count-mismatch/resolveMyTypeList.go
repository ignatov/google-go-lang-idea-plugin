package docker1

type iface interface {}

type baseStuct struct {}

type myStruct struct {
	baseStuct
}

func (base baseStuct) Handle(b byte) (s iface, e error) {
	return nil, nil
}

func (ms myStruct) Handle(b byte) {
	ns, err := ms.baseStuct.Handle(b)
	_, _ = ns, err
}
