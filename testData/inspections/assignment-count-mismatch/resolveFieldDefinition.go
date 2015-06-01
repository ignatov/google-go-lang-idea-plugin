package docker9

type Archiver struct {
	Untar func() error
}

func _(archiver *Archiver) (err error) {
	err = archiver.Untar()
	return
}
