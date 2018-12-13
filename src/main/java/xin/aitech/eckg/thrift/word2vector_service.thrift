namespace java xin.aitech.eckg.thrift.word2vector // defines the namespace

struct w2vPayload{
1: list<string> words,
2: i32 topn,
3: string language,
}

struct similarWords{
1: list<string> words,
}

service w2vCnEnService{
        list<similarWords> process(1:w2vPayload content),
}
