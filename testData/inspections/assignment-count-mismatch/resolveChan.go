package docker6

func Dem() (chan interface{}) {
	return make(chan interface{})
}

func _() (c chan interface{}) {
	c = Dem()
	return
}
