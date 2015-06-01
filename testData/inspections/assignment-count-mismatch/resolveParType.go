package assignmentCount

func _(yup int) (img int) {
	type demo struct{}

	f := *(*func(imgId string, width, height int) demo)(yup)

	img = f(1, 1)
	return
}

func _() {
	for _, fn := range [](func(int, interface{}, interface{}) error){} {
		if err := fn(11, 2, 3); err != nil {
			return err
		}
	}
}
