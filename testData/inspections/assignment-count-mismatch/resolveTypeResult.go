package assignmentCount

type sinkFactoryExt struct {}
type SinkFactory func() ([]string, error)

type dem struct{
	Hello *sinkFactoryExt
}

func (ep *sinkFactoryExt) Lookup(name string) SinkFactory {
	return SinkFactory{}
}

func _() (factory int, err error) {
	factory, err = dem.Hello.Lookup("dea")()
	factory = dem.Hello.Lookup("demo")
	return
}

