namespace java xin.aitech.eckg.thrift.searchKS // defines the namespace

struct EventPayload{
1: string objectUrl,
2: string objectLabel,
3: string verbUrl,
4: string verbLabel,
5: string subjectUrl,
6: string subjectLabel,
7: string inDateTimeUrl,
8: string beginningTimeUrl,
9: string endTimeUrl,
}

struct ReceivePayload{
1: string traceId,
2: string language,
3: string content,
4: map<string, string> settingVar,
}

service SearchKSService{
        list<EventPayload> process(1: ReceivePayload receivePayload),
}
