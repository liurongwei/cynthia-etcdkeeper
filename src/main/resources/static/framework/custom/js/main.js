var stateStore = {
    server: null,
    treeMode: 'path',
    editor: null,
    aceMode: 'text',
    tree: [],
    idCount: 0,
    curIconMode: 'mode_icon_text',
    separator: '/',
    idCount: 0
};

var etcdService = {
    option: {
        serverBase: '/api/etcd'
    },
    separator: function () {
        var self = this;
        $.ajax({
            type: 'GET',
            timeout: 5000,
            url: self.option.serverBase + '/separator',
            async: false,
            success: function (data) {
                var separator = data.data;
                stateStore.separator = separator;
                console.log(data);
            },
            error: function (err) {
                $.messager.alert('Error', $.toJSON(err), 'error');
            }
        });
    },
    connect: function (record) {
        var self = this;
        var result = null;
        if (!record) {
            record = stateStore.server;
        }
        if (!record) return false;

        if (record.apiVersion == '2') {
            stateStore.treeMode = 'path';
        }

        $.ajax({
            method: 'post',
            contentType: "application/json",
            url: self.option.serverBase + '/connect',
            data: JSON.stringify(record),
            dataType: 'json',
            async: true,
            success: function (res) {
                console.log(res);
                result = res;
                if (res.status == 0) {
                    if (res.data) {
                        $('#statusVersion').html('ETCD version:' + res.data.version);
                        $('#statusSize').html('Size:' + res.data.size)
                        $('#statusMember').html('Member name:' + res.data.name)
                    } else {
                        $('#statusVersion').html('');
                        $('#statusSize').html('')
                        $('#statusMember').html('')
                    }
                } else {
                    $.messager.alert('Error', data.message, 'error');
                }
            },
            error: function (err) {
                $.messager.alert('Error', $.toJSON(err), 'error');
            },
            complete: function (jqXHR, textStatus) {
                if (result && result.status === 0) {
                    reload();
                } else {
                    resetValue();
                    $('#etree').tree('loadData', []);
                }
            }
        });
    },
    path: function () {

    },
    key: function () {

    }
};

var messageService = {
    alert: function (msg) {
        $.messager.show({
            title: 'Message',
            msg: msg,
            showType: 'slide',
            timeout: 1000,
            style: {
                right: '',
                bottom: ''
            }
        });
    }
};

$('#server-list').combobox({
    url: '/api/etcd/servers',
    method: 'get',
    valueField: 'id',
    textField: 'title',
    panelHeight: 'auto',
    label: 'servers:',
    labelPosition: 'top',
    loadFilter: function (data) {
        return data.data;
    },
    formatter: function (row) {
        var opts = $(this).combobox('options');
        return row.name + '(' + row[opts.textField] + ')';
    },
    onSelect: function (record) {
        stateStore.server = record;
        etcdService.connect(record);
    }
});


function resizeWindow() {
    $('#elayout').height(($(window).height() - 128) + 'px')
}

// init ui data
(function init() {
    resizeWindow();
    $(window).resize(function () { // FIXME: invalid
        resizeWindow();
    });

    var editor = ace.edit('value');
    editor.$blockScrolling = Infinity;
    stateStore.editor = editor;

    var aceMode = Cookies.get('ace-mode');
    if (typeof (aceMode) === 'undefined') {
        aceMode = 'text';
    }
    stateStore.aceMode = aceMode;

    var treeMode = Cookies.get('tree-mode');
    if (typeof (treeMode) != 'undefined' && treeMode != '') {
        stateStore.treeMode = treeMode;
    }


    // get separator
    etcdService.separator();
})();

$(document).ready(function () {
    stateStore.editor.setTheme('ace/theme/github');
    stateStore.editor.getSession().setMode('ace/mode/' + stateStore.aceMode);
    changeMode(stateStore.aceMode);

    var t = $('#etree').tree({
        animate: true,
        onClick: showNode,
        //lines:true,
        onContextMenu: showMenu
    });
});

function getId() {
    return stateStore.idCount++;
}

function reload() {
    console.log(stateStore.separator);
    var separator = stateStore.separator;
    var rootNode = {
        id: getId(),
        children: [],
        dir: true,
        path: separator,
        text: separator,
        iconCls: 'icon-dir'
    };
    tree = [];
    tree.push(rootNode);
    $('#etree').tree('loadData', tree);
    showNode($('#etree').tree('getRoot'));
    resetValue();
}

function resetValue() {
    var editor = stateStore.editor;
    var separator = stateStore.separator;
    $('#elayout').layout('panel', 'center').panel('setTitle', separator);
    editor.getSession().setValue('');
    editor.setReadOnly(false);
    $('#footer').html('&nbsp;');
}

function changeTreeMode() {
    if (!stateStore.server) return;
    if (stateStore.server.apiVersion === '2') {
        stateStore.treeMode = 'path';
        messageService.alert('Etcd v2 only supports directory mode.');
    } else {
        if (stateStore.treeMode === 'list') {
            stateStore.treeMode = 'path';
        } else {
            stateStore.treeMode = 'list';
        }
        Cookies.set('tree-mode', stateStore.treeMode, {expires: 30});
        etcdService.connect();
    }
}

function changeMode(mode) {
    stateStore.aceMode = mode;
    Cookies.set('ace-mode', stateStore.aceMode, {expires: 30});
    $('#' + stateStore.curIconMode).remove();
    stateStore.editor.getSession().setMode('ace/mode/' + stateStore.aceMode);
    stateStore.curIconMode = 'mode_icon_' + stateStore.aceMode;
    $('#mode_' + mode).append('<div id="' + stateStore.curIconMode + '" class="menu-icon icon-ok"></div>');
    $('#showMode').html(stateStore.aceMode);
}

function format(type) {
    if (type === 'json') {
        val = JSON.parse(editor.getValue());
        editor.setValue(JSON.stringify(val, null, 4));
        editor.getSession().setMode('ace/mode/' + 'json');
        editor.clearSelection();
        editor.navigateFileStart();
    }
}

function changeFooter(ttl, cIndex, mIndex) {
    $('#footer').html('<span>TTL&nbsp;:&nbsp;' + ttl
        + '&nbsp;&nbsp;&nbsp;&nbsp;CreateRevision&nbsp;:&nbsp;'
        + cIndex + '&nbsp;&nbsp;&nbsp;&nbsp;ModRevision&nbsp;:&nbsp;'
        + mIndex + '</span><span id="showMode" style="position: absolute;right: 10px;color: #777;">'
        + stateStore.aceMode + '</span>');
}

function format(type) {
    if (!type) {
        type = stateStore.aceMode;
    }
    var editor = stateStore.editor;
    if (type === 'json') {
        try {
            val = JSON.parse(editor.getValue());
            editor.setValue(JSON.stringify(val, null, 4));
            editor.getSession().setMode('ace/mode/' + 'json');
            editor.clearSelection();
            editor.navigateFileStart();
        } catch (e) {
            console.log(e);
        }
    }
}

function selDir(item) {
    if (item.value === 'true') {
        $('#cvalue').textbox('disable', 'none');
    } else {
        $('#cvalue').textbox('enable', 'none');
    }
}

function showNode(node) {
    if (!stateStore.server) return;
    var version = stateStore.server.apiVersion;
    var treeMode = stateStore.treeMode;
    var editor = stateStore.editor;
    var serverBase = etcdService.option.serverBase;
    $('#elayout').layout('panel', 'center').panel('setTitle', node.path);
    editor.getSession().setValue('');
    if (node.dir === false) {
        editor.setReadOnly(false);
        $.ajax({
            type: 'GET',
            timeout: 5000,
            url: etcdService.option.serverBase + '/key',
            data: {'key': node.path, serverId: stateStore.server.id},
            async: true,
            dataType: 'json',
            success: function (res) {
                if (res.status != 0) {
                    $('#etree').tree('remove', node.target);
                    console.log(res.message);
                    resetValue()
                } else {
                    editor.getSession().setValue(res.data.value);
                    //if (autoFormat === 'true') {
                    //format(aceMode);
                    //}
                    var ttl = 0;
                    if (res.data.ttl) {
                        ttl = res.data.ttl;
                    }
                    changeFooter(ttl, res.data.createdIndex, res.data.modifiedIndex);
                    changeModeBySuffix(node.path);
                }
            },
            error: function (err) {
                $.messager.alert('Error', $.toJSON(err), 'error');
            }
        });
    } else {
        if (node.children.length > 0) {
            $('#etree').tree(node.state === 'closed' ? 'expand' : 'collapse', node.target);
        }
        if (version === '2') {
            if (node.state === 'closed') {
                return
            }
            editor.setReadOnly(true);
        }

        $('#footer').html('&nbsp;');
        // clear child node
        var children = $('#etree').tree('getChildren', node.target);
        //if (node.state === 'closed' || children.length === 0) {

        //}
        var url = '';
        if (treeMode === 'list') {
            url = serverBase + '/key';
        } else {
            url = serverBase + '/path';
        }
        $.ajax({
            type: 'GET',
            timeout: 5000,
            url: url,
            data: {'key': node.path, 'prefix': 'true', serverId: stateStore.server.id},
            async: true,
            dataType: 'json',
            success: function (res) {
                if (res.status != 0) {
                    $.messager.alert('Error', data.message, 'error');
                    return;
                }
                if (res.data) {
                    if (res.data.value) {
                        editor.getSession().setValue(res.data.value);
                        changeFooter(res.data.ttl, res.data.createdIndex, res.data.modifiedIndex);
                        changeModeBySuffix(node.path);
                    }
                    var arr = [];

                    if (res.data.nodes) {
                        // refresh child node
                        for (var i in res.data.nodes) {
                            var newData = getNode(res.data.nodes[i]);
                            arr.push(newData);
                        }
                        $('#etree').tree('append', {
                            parent: node.target,
                            data: arr
                        });
                    }

                    for (var n in children) {
                        $('#etree').tree('remove', children[n].target);
                    }
                }
            },
            error: function (err) {
                $.messager.alert('Error', $.toJSON(err), 'error');
            }
        });
    }
}

function saveValue() {
    if (!stateStore.server) return;
    var serverBase = etcdService.option.serverBase;
    var editor = stateStore.editor;
    var node = $('#etree').tree('getSelected');
    console.log(node);
    if (node.dir === true) {
        $.messager.alert('Error', "key \"" + node.path + "\" is a dir,cann't be edit", 'error');
        return;
    }
    $.ajax({
        type: 'PUT',
        contentType: "application/json",
        timeout: 5000,
        url: serverBase + '/put',
        data: JSON.stringify({'key': node.path, 'value': editor.getValue(), serverId: stateStore.server.id}),
        async: true,
        dataType: 'json',
        success: function (res) {
            if (res.status != 0) {
                $.messager.alert('Error', res.message, 'error');
            } else {
                editor.getSession().setValue(res.data.value);
                var ttl = 0;
                if (res.data.ttl) {
                    ttl = res.data.ttl;
                }
                changeFooter(ttl, res.data.createdIndex, res.data.modifiedIndex);
                messageService.alert('Save success.');
            }
        },
        error: function (err) {
            $.messager.alert('Error', $.toJSON(err), 'error');
        }
    });
}

function getNode(n) {
    var treeMode = stateStore.treeMode;
    var separator = stateStore.separator;
    var text = '';
    if (treeMode === 'list') {
        text = n.key;
    } else {
        var path = n.key.split(separator);
        text = path[path.length - 1];
    }
    var obj = {
        id: getId(),
        text: text,
        dir: false,
        iconCls: 'icon-text',
        path: n.key,
        children: []
    };
    if (n.dir === true) {
        obj.state = 'closed';
        obj.dir = true;
        obj.iconCls = 'icon-dir';
        if (n.nodes) {
            for (var i in n.nodes) {
                var rn = getNode(n.nodes[i]);
                obj.children.push(rn);
            }
        }
    }
    return obj
}

function changeModeBySuffix(path) {
    var separator = stateStore.separator;
    var a = path.split(separator);
    var tokens = a.slice(a.length - 1, a.lenght)[0].split('.');
    if (tokens.length < 2) {
        return
    }
    var mode = tokens[tokens.length - 1];
    var modes = $('#modeMenu').children();
    for (var i = 0; i < modes.length; i++) {
        m = modes[i].innerText;
        if (mode === m) {
            changeMode(m);
            return
        }
    }
}

function showMenu(e, node) {
    e.preventDefault();
    if (!stateStore.server) return;
    $('#etree').tree('select', node.target);
    var mid = "treeNodeMenu";
    if (stateStore.treeMode === 'path') {
        mid = 'treeDirMenu';
        if (stateStore.server.apiVersion === '2') {
            if (node.dir !== true) {
                mid = "treeNodeMenu";
            }
        }
    } else {
        if (node.dir === true) {
            mid = 'treeDirMenu';
        }
    }
    $('#' + mid).menu('show', {
        left: e.pageX,
        top: e.pageY
    });
}

function createNode() {
    if (!stateStore.server) return;
    var separator = stateStore.separator;
    var serverId = stateStore.server.id;
    var treeMode = stateStore.treeMode;
    var serverBase = etcdService.option.serverBase;
    var version = stateStore.server.apiVersion;
    var node = $('#etree').tree('getSelected');
    var nodePath = node.path;
    if (nodePath === separator) {
        nodePath = ''
    }

    if (treeMode == 'list') { // list mode
        if ($('#cnodeForm').form('validate')) {
            var createNodePath = $('#name').textbox('getValue');
            if (!createNodePath.startsWith(separator)) {
                createNodePath = separator + $('#name').textbox('getValue');
            }
            $.ajax({
                type: 'PUT',
                contentType: "application/json",
                timeout: 5000,
                url: serverBase + '/put',
                data: JSON.stringify({
                    'key': createNodePath,
                    'value': $('#cvalue').textbox().val(),
                    'ttl': $('#ttl').numberbox().val(),
                    serverId: serverId
                }),
                async: true,
                dataType: 'json',
                success: function (res) {
                    $('#cnode').window('close');
                    if (res.status != 0) {
                        $.messager.alert('Error', res.message, 'error');
                    } else {
                        messageService.alert('Create success.');
                        var newData = [];
                        var obj = {
                            id: getId(),
                            text: createNodePath,
                            state: $('#dir').combobox('getValue') === 'true' ? 'closed' : '',
                            dir: $('#dir').combobox('getValue') === 'true',
                            iconCls: $('#dir').combobox('getValue') === 'true' ? 'icon-dir' : 'icon-text',
                            path: createNodePath,
                            children: []
                        };
                        var objNode = nodeExist(obj.path);
                        if (objNode === null) {
                            newData.push(obj);

                            $('#etree').tree('append', {
                                parent: node.target,
                                data: newData
                            });
                        }
                    }
                    $('#cvalue').textbox('enable', 'none');
                    $('#cnodeForm').form('reset');
                    $('#ttl').numberbox('setValue', '');
                },
                error: function (err) {
                    $.messager.alert('Error', err, 'error');
                }
            });
        }
    } else { // dir mode
        if ($('#cnodeForm').form('validate')) {
            var pathArr = [];
            var inputArr = $('#name').textbox('getValue').split(separator);
            for (var i in inputArr) {
                if ($.trim(inputArr[i]) != '') {
                    pathArr.push(inputArr[i]);
                }
            }

            $.ajax({
                type: 'PUT',
                contentType: "application/json",
                timeout: 5000,
                url: serverBase + '/put',
                data: JSON.stringify({
                    dir: $('#dir').combobox('getValue'),
                    'key': nodePath + separator + pathArr.join(separator),
                    'value': $('#cvalue').textbox().val(),
                    'ttl': $('#ttl').numberbox().val(),
                    serverId: serverId
                }),
                async: true,
                dataType: 'json',
                success: function (ret) {
                    $('#cnode').window('close');
                    if (ret.status != 0) {
                        $.messager.alert('Error', ret.message, 'error');
                    } else {
                        messageService.alert('Create success.');
                        var newData = [];
                        var preObj = {};
                        var prePath = node.path;
                        for (var k in pathArr) {
                            var obj = {};
                            if (k == pathArr.length - 1) {
                                obj = {
                                    id: getId(),
                                    text: pathArr[k],
                                    state: $('#dir').combobox('getValue') == 'true' ? 'open' : '',
                                    dir: $('#dir').combobox('getValue') == 'true' ? true : false,
                                    iconCls: $('#dir').combobox('getValue') == 'true' ? 'icon-dir' : 'icon-text',
                                    path: (prePath == separator ? (prePath + '') : (prePath + separator)) + pathArr[k],
                                    children: []
                                };
                            } else {
                                obj = {
                                    id: getId(),
                                    text: pathArr[k],
                                    state: 'closed',
                                    dir: true,
                                    iconCls: 'icon-dir',
                                    path: (prePath == separator ? (prePath + '') : (prePath + separator)) + pathArr[k],
                                    children: []
                                };
                            }
                            var objNode = nodeExist(obj.path);
                            if (objNode != null) {
                                node = objNode;
                                prePath = node.path;
                                continue;
                            }
                            if (newData.length === 0) {
                                newData.push(obj);
                            } else {
                                preObj.children.push(obj);
                            }
                            preObj = obj;
                            prePath = obj.path;
                        }
                        if (version === '3') {
                            $('#etree').tree('update', {
                                target: node.target,
                                iconCls: 'icon-dir'
                            });
                        }
                        $('#etree').tree('append', {
                            parent: node.target,
                            data: newData
                        });
                    }

                    $('#cvalue').textbox('enable', 'none');
                    $('#cnodeForm').form('reset');
                    $('#ttl').numberbox('setValue', '');
                },
                error: function (err) {
                    $.messager.alert('Error', err, 'error');
                }
            });
        }
    }
}

function nodeExist(p) {
    for (var i = 0; i <= stateStore.idCount; i++) {
        var node = $('#etree').tree('find', i);
        if (node !== null && node.path === p) {
            return node;
        }
    }
    return null;
}

function removeNode() {
    if (!stateStore.server) return;
    var serverBase = etcdService.option.serverBase;
    var node = $('#etree').tree('getSelected');
    var serverId = stateStore.server.id;
    var version = stateStore.server.apiVersion;
    var separator = stateStore.separator;
    $.messager.confirm('Confirm', 'Remove ' + node.text + '?', function (r) {
        if (r) {
            $.ajax({
                type: 'DELETE',
                contentType: "application/json",
                timeout: 5000,
                url: serverBase + '/delete',
                data: JSON.stringify( {'key': node.path, 'dir': node.dir, serverId: serverId}),
                async: true,
                dataType: 'json',
                success: function (res) {
                    resetValue();
                    if (res.status === 0) {
                        messageService.alert('Delete success.');

                        var pnode = $('#etree').tree('getParent', node.target);

                        $('#etree').tree('remove', node.target);

                        if (version === '3') {
                            var isLeaf = $('#etree').tree('isLeaf', pnode.target);
                            if (isLeaf && pnode.text !== separator) {
                                $('#etree').tree('update', {
                                    target: pnode.target,
                                    iconCls: 'icon-text'
                                });
                            }
                        }
                    } else {
                        $.messager.alert('Error', data, 'error');
                    }
                },
                error: function (err) {
                    $.messager.alert('Error', $.toJSON(err), 'error');
                }
            });
        }
    });
}
