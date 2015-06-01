package assignmentCount

type field struct {
	nameBytes []byte // []byte(name)
	equalFold func(s, t []byte) bool
}

func foldFunc(s []byte) func(s, t []byte) bool {
	return
}

func _(f field) field {
	f.equalFold = foldFunc(f.nameBytes)
	return
}

type Transport struct{}

func (t *Transport) dialClientConn(addr string) (demo1, demo2 int) {
	demo1, demo2 = t.dialTLS()("demo")
	return
}

func (t *Transport) dialTLS() func(string) (demo1, demo2 int) {
	return t.dialTLSDefault
}

func (t *Transport) dialTLSDefault(string) (int, int) {
	return 1, 2
}

func _() {
	_ = TimeoutDialer(1, "2")
}

func TimeoutDialer(cTimeout int, rwTimeout string) func(net, addr string) (c int, err error) {
	return nil
}

type tabDirection int
type State struct{}

func (s *State) circularTabs(items []string) func(tabDirection) (string, error) {
	return func(direction tabDirection) (string, error) {
		return "", nil
	}
}

func (s *State) _(list []string, direction tabDirection) {
	tabPrinter := s.circularTabs(list)
	_, _ = tabPrinter(direction)
}

type demo struct{}
type demo2 struct {
	reg demo
}

func (t *demo) dem() (undo func()) {
	return func() {}
}

func _(s demo2) {
	_ = s.reg.dem()
}
