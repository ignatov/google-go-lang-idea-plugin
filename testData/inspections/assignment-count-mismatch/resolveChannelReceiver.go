package docker11

type Tx struct{}

func (tx *Tx) Check() <-chan error {
	return make(chan error)
}

func _(tx *Tx) (err error) {
	if err, ok := <-tx.Check(); ok {
		panic("check faaail: " + err.Error())
	}
	return
}

type Demo struct{
	msg chan interface{}
}

func _(ch Demo) (m string, ok bool) {
	m, ok = (<-ch.msg)
	return
}

