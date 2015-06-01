package assignmentCount

func _() {
	var c, d = func() (int, int, int) {return 1, 1, 1}, 1
	_, _ = c, d
}

func _() {
	var <error descr="Assignment count mismatch: 3 element(s) assigned to 2 element(s)">c, d = <error descr="Multiple-value func() (int, int, int) in single-value context">func() (int, int, int) {return 1, 1, 1}()</error></error>
	_, _ = c, d
}

func _() {
	var c, d = func() (int, int) {return 1, 1}()
	_, _ = c, d
}

func _() {
	var <error descr="Assignment count mismatch: 3 element(s) assigned to 2 element(s)">c, d = func() (int, int) {return 1, 1}, 1, 2</error>
	_, _ = c, d
}

func _() {
	var e, f = <error descr="Multiple-value func() (int, int) in single-value context">func() (int, int) {return 1, 1}()</error>, 1
	_, _ = e, f
}

type validate interface {
	validate() (bool, error)
}

type user struct {
}

func (u user) validate() (bool, error) {
	return false, nil
}

func main() {
	var s validate

	s = user{}
	if <error descr="Assignment count mismatch: 2 element(s) assigned to 1 element(s)">err := <error descr="Multiple-value s.validate() in single-value context">s.validate()</error></error>; err != nil { // Should reports assignment count mismatch error
		return
	}
}