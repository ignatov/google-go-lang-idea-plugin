package assignmentCount

type pc func() (pwd string, err error)

func (cb pc) _() (pw int, err error) {
	pw, err = cb()
	return
}
