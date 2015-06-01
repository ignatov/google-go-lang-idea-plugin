package assignmentCount

type IsolatorValue interface {}

type IsolatorValueConstructor func() IsolatorValue

var (
	isolatorMap map[int]IsolatorValueConstructor
)

func _(yup int) (isol int) {
	isol = isolatorMap[yup]()
	return
}

type index interface {
	Compact(rev int64) map[int]struct{}
}

type store struct {
	kvindex index
}

func (s *store) _(yup int) (keep int) {
	keep = s.kvindex.Compact(yup)
	return
}
