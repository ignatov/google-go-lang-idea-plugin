package assignmentCount

type Cache interface {
	GetPods() []*string
}

type externalSinkManager struct {
	cache Cache
}

func _(esm *externalSinkManager) (pods int) {
	pods = esm.cache.GetPods()
	return
}

var run = []func(int, int, string) ([]int, string){}

func _() {
	for i := range []int{} {
		_, _ = run[i](4, 4, "3")
	}
}
