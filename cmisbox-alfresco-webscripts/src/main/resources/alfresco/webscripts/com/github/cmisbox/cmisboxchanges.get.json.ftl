<#escape x as jsonUtils.encodeJSONString(x)>
{
        "token":${token},
        "events":[
           <#list events as e>
            {"t":"${e[0]}", "id":"${e[1]}"},
            </#list>
        ]
}
</#escape>
