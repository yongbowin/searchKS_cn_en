namespace java xin.aitech.eckg.thrift.ltp // defines the namespace

struct Role{
1: string role,
2: i32 startIndex,
3: i32 endIndex,
}

struct Srl{
1: i32 pos,
2: list<Role> roles,
}

struct LtpPayload{
1: list<string> words,
2: list<string> postags,
3: list<string> ners,
4: list<i32> heads,
5: list<string> deprels,
6: list<Srl> srls,
}

service LtpService{
        list<LtpPayload> process(1:string content, 2:string traceId),
        list<LtpPayload> segment(1:string content, 2:string traceId),
}
