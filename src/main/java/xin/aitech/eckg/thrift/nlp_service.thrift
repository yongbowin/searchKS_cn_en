namespace java xin.aitech.eckg.thrift.nlp // defines the namespace

struct Srl{
1: string a1,
2: string a0,
3: string v,
}

struct PosNerChunk{
1: string word,
2: string tag,
}

struct NlpPayload{
1: list<string> words,
2: list<PosNerChunk> pos,
3: list<PosNerChunk> ner,
4: list<PosNerChunk> chunk,
5: list<string> verbs,
6: list<Srl> srl,
}

service NlpService{
        list<NlpPayload> process(1:string content, 2:string traceId),
}
