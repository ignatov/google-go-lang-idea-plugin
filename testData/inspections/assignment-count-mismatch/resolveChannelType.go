package docker8

type Publisher struct{}

type Events struct {
	pub *Publisher
}

func (p *Publisher) Subscribe() chan interface{} {
	return make(chan interface{})
}

func (e *Events) Subscribe() (l chan interface{}) {
	l = e.pub.Subscribe()
	return
}
