/**
 * Copyright (C) 2011, 2012 Alejandro Ayuso
 *
 * This file is part of Jongo.
 * Jongo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * Jongo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jongo.  If not, see <http://www.gnu.org/licenses/>.
 */

function getJongoStats(){
    debug('Obtaining Jongo Stats');
    var stats = new Array();
    $.ajax({
        url: '/adminws/stats',
        async: false,
        dataType: 'json',
        success: function(data){
            stats = data.response
        }
    })
    return stats;
}

function drawJongoStats(component){
    var stats = getJongoStats();
    var container = $(component);
    $(container).html('');
    var output = new Array();
    output.push('<table class="meta-data-table ui-corner-all" style="margin-bottom: 10px"><tr><th>Concept</th><th>Value</th><tr>')
    output.push('<tr><td>Total Served Requests</td><td>');
    output.push(stats.total);
    output.push('</td><tr><tr><td>Succesful Requests</td><td>');
    output.push(stats.success);
    output.push('</td></tr><tr><td>Failed Requests</td><td>');
    output.push(stats.fail);
    output.push('</td></tr><tr><td>Create Requests</td><td>');
    output.push(stats.create);
    output.push('</td></tr><tr><td>Read Requests</td><td>');
    output.push(stats.read);
    output.push('</td></tr><tr><td>Update Requests</td><td>');
    output.push(stats.update);
    output.push('</td></tr><tr><td>Delete Requests</td><td>');
    output.push(stats['delete']);
    output.push('</td></tr><tr><td>Dynamic Queries</td><td>');
    output.push(stats.dynamic);
    output.push('</td></tr><tr><td>Complex Queries</td><td>');
    output.push(stats.query);
    output.push('</td></tr>');
    output.push('</table><form><input type="submit" class="jquery-button" value="Refresh" onclick="drawJongoStats(\'');
    output.push(component);
    output.push('\'); return false;"/></form>');
    $(container).html(output.join(''));
    $( '.jquery-button' ).button();
}

