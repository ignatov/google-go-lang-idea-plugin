package docker10

func _(archiver *Archiver) (err error) {
	err = archiver.Untar()
	return
}
