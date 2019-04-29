

$('#server-list').combobox({
    url:'/api/etcd/servers',
    method:'get',
    valueField:'id',
    textField:'title',
    panelHeight:'auto',
    label: 'servers:',
    labelPosition: 'top',
    loadFilter: function(data){
        return data.data;
    },
    formatter: function(row){
        var opts = $(this).combobox('options');
        return row[opts.textField];
    },
    onSelect: function (record) {
        $.ajax({
            method:'post',
            contentType:"application/json",
            url:'/api/etcd/connect',
            data: JSON.stringify( record),
            dataType:'json',
            success:function (response) {
                console.log(response);
            }
        })
    }
});
