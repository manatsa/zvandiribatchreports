<%@include file="./template/header.jspf" %>
<div class="col-lg-12">
    <div class="panel panel-default">
        <div class="panel-heading">
            ${pageTitle}
        </div>
        <div class="panel-body">
            <div class="col-md-12">
                <div class="col-md-3">
                    <a href="${page}/report/index.htm">&DoubleLeftArrow; Reports DashBoard</a><br/>
                </div>
<%--                <div class="col-lg-1" />--%>
                <div class="col-md-9">
                    <div class="col-md-10">
                        <div id="Progress_Status">
                            <div id="myprogressBar"></div>
                        </div>
                    </div>
                </div>
            </div>
            <form:form modelAttribute="item" role="form" onsubmit="runExportPatientsProgress()">
                <table class="table">
                    <tbody>
                        <tr>
                            <c:if test="${userLevel == 'NATIONAL'}">
                                <td>
                                    <div class="form-group">
                                        <label for="provinces">Region</label>
                                        <form:select path="provinces" size="15" class="form-control" id="provinces" multiple="multiple">
                                            <form:option value="" label="--Select Item--" />
                                            <form:options items="${provinces}" itemLabel="name" itemValue="id"/>
                                        </form:select>
                                        <p class="help-block">
                                            <form:errors path="provinces" class="alert-danger"/>
                                        </p>
                                    </div> 
                                </td>
                            </c:if>
                            <c:if test="${userLevel == 'NATIONAL' or userLevel == 'PROVINCE'}">
                                <td>
                                    <div class="form-group">
                                        <label for="districts">District</label>
                                        <form:select path="districts" size="15" class="form-control" id="districts" multiple="multiple">
                                            <form:option value="" label="--Select Item--"/>
                                            <form:options items="${districts}" itemLabel="name" itemValue="id"/>
                                        </form:select>
                                        <p class="help-block">
                                            <form:errors path="districts" class="alert-danger"/>
                                        </p>
                                    </div> 
                                </td>
                            </c:if>
                            <td>
                                <div class="form-group">
                                    <label for="facilities">Facility</label>
                                    <form:select path="facilities" size="18" class="form-control" id="facilities" multiple="multiple">
                                        <form:option value="" label="--Select Item--"/>
                                        <form:options items="${facilities}" itemLabel="name" itemValue="id"/>
                                    </form:select>
                                    <p class="help-block">
                                        <form:errors path="facilities"/>
                                    </p>
                                </div> 
                            </td>
                            <td>
                                <div class="form-group">
                                    <label>Patient Statuses</label>
                                    <form:select path="statuses" class="form-control" size="15">
                                        <form:option value="" label="--Select Item--" />
                                        <form:options items="${statuses}"  itemLabel="name" itemValue="code" />
                                    </form:select>
                                    <p class="help-block">
                                        <form:errors path="statuses" />
                                    </p>
                                </div>
                            </td>
                            <td>
                                <div class="form-group">
                                    <label>&nbsp;</label><br/>
                                    <button class="btn btn-primary" type="submit"  >Export</button>
                                </div> 
                            </td>
                        </tr>
                    </tbody>
                </table>
            </form:form>
            <div class="row">
            </div>

        </div>
        <div class="panel-footer" style="text-align: right">

        </div>
    </div>
</div>
<%@include file="./template/footer.jspf" %>
<script type="text/javascript">
    $("#toggle-rem-side-bar").click()
    $("#item").validate({
       rules: {
           province: {
               required: true
           }
       } 
    });
</script>
