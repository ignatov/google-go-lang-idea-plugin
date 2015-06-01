package assignmentCount

type frameParser func() (int, error)

func typeFrameParser() frameParser {
	return nil
}

func _() (f int, err error) {
	f, err = typeFrameParser()()
	return
}
