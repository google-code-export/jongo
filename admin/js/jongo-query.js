function getJongoQueries(){
    debug('Obtaining Jongo Queries');
    var queries = new Array();
    $.ajax({
        url: '/adminws/query/all',
        async: false,
        dataType: 'json',
        success: function(data){
            queries = data.response
        }
    })
    return queries;
}

function drawJongoQueries(component, queries){
    if(queries == null){
        queries = getJongoQueries();
    }
    $(component).accordion("destroy");
    var items = new Array();
    $.each(queries, function(){
        items.push('<h3 id="h3_');
        items.push(this.id);
        items.push('"><a href="#">');
        items.push(this.name);
        items.push('</a></h3><div id="queryId_');
        items.push(this.id);
        items.push('"><p>');
        items.push(this.description);
        items.push('</p><p><textarea class="jongo-text-area" rows="10" readonly="true">');
        items.push(this.query);
        items.push('</textarea></p><form>');
        items.push('<input type="hidden" value="');
        items.push(this.name);
        items.push('" id="query');
        items.push(this.id);
        items.push('">');
        items.push('<input type="submit" class="jquery-button" value="Delete" onclick="deleteQuery(');
        items.push(this.id);
        items.push('); return false;"/>');
        items.push('</form></div>');
    });
    $(component).html(items.join(''));
    $(component).accordion({active: false, autoHeight: false, navigation: true, collapsible: true});
    $('.jquery-button').button();
}

function addQueryToAccordion(component, id, name, description, query){
    if(component == null){
        component = '#jqueries';
    }
    
    var items = new Array();
    items.push('<h3 id="h3_');
    items.push(id);
    items.push('"><a href="#">');
    items.push(name);
    items.push('</a></h3><div id="queryId_');
    items.push(id);
    items.push('"><p>');
    items.push(description);
    items.push('</p><p><textarea class="jongo-text-area" rows="10" readonly="true">');
    items.push(query);
    items.push('</textarea></p><form>');
    items.push('<input type="hidden" value="');
    items.push(name);
    items.push('" id="query');
    items.push(id);
    items.push('">');
    items.push('<input type="submit" class="jquery-button" value="Delete" onclick="deleteQuery(');
    items.push(id);
    items.push('); return false;"/>');
    items.push('</form></div>');
    
    $(component).append(items.join(''));
    $(component).accordion("destroy");
    $(component).accordion({
                autoHeight: false,
                navigation: true,
                collapsible: true
    });
}

function addQuery(){
    var data = {}
    data['name'] = $("#queryName").val();
    data['description'] = $("#queryDescription").val();
    var queryText = $("#queryText").val()
    data['query'] = queryText.replace(/(\r\n|\n|\r)/gm,"\\n"); // chrome doesn't like line breaks in json
    
    var ret = $.post('/adminws/query', data, function() {}, 'json');
    
    ret.success(function(){
        showJQueryDialog("Successfully added new query", data['name']);
        drawJongoQueries('#jqueries');
    })
    
    ret.error(function(error){
        var jongoError = JSON.parse(error.responseText);
        showJQueryDialog("Error " + error.status, jongoError.response.message);
    });
}

function deleteQuery(id){
    var queryName = $("#query" + id).val();
    
    $("#confirmDialogMessage").text('Are you sure you wish to continue and delete the query?')
    $( "#confirmDialog" ).dialog({
        resizable: false,
        height:200,
        width: 300,
        modal: true,
        title: 'Delete Query ' + queryName,
        buttons: {
            Ok: function() {
                var ret = $.ajax({
                    type: 'DELETE',
                    url: '/adminws/query/' + id,
                    success: function() {},
                    dataType: 'json'
                });

                ret.success(function(){
                    showJQueryDialog("Successfully deleted query", queryName);
                    drawJongoQueries('#jqueries');
                })

                ret.error(function(error){
                    var jongoError = JSON.parse(error.responseText);
                    showJQueryDialog("Error " + error.status, jongoError.response.message);
                });
                $( this ).dialog( "close" );
            },
            Cancel: function() {
                $( this ).dialog( "close" );
            }
        }
    });
}