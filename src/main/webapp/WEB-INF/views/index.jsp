<%@include file="./template/header.jspf"%>
<form:form>
    <legend style="color: green">Reports Dash Board</legend>

    <div class="col-lg-12">
        <div class="panel panel-default">
            <div class="panel-heading">View and Export Aggregate Reports</div>
            <div class="panel-body">
                <div class="table-responsive">
                    <table width="100%" class="table table-bordered">
                        <tbody>

                        <tr>
                            <td><sec:authorize access="
                                                   hasRole('ROLE_ADMINISTRATOR') or
                                                   hasRole('ROLE_DATA_CLERK') or
                                                   hasRole('ROLE_M_AND_E_OFFICER') or
                                                   hasRole('ROLE_HOD_M_AND_E') or
                                                   hasRole('ROLE_ZM') or
                                                   hasRole('ROLE_ZI')
                                                   ">
                                <a href="${page}/report/hierarchical-database-export/index">Hierarchical Database Export</a>
                            </sec:authorize></td>
                            <td><sec:authorize access="
                                                   hasRole('ROLE_ADMINISTRATOR') or
                                                   hasRole('ROLE_DATA_CLERK') or
                                                   hasRole('ROLE_M_AND_E_OFFICER') or
                                                   hasRole('ROLE_HOD_M_AND_E') or
                                                   hasRole('ROLE_ZM') or
                                                   hasRole('ROLE_ZI')
                                                   ">
                                <a href="${page}/report/case-load-management/index.htm">Caseload Management Plan</a>
                            </sec:authorize></td>
                        </tr>
                        <tr>
                            <td><sec:authorize access="
                                                   hasRole('ROLE_ADMINISTRATOR') or
                                                   hasRole('ROLE_DATA_CLERK') or
                                                   hasRole('ROLE_M_AND_E_OFFICER') or
                                                   hasRole('ROLE_HOD_M_AND_E') or
                                                   hasRole('ROLE_ZM') or
                                                   hasRole('ROLE_ZI')
                                                   ">
                                <a href="${page}/report/contact/index.htm">Contact Detailed Report</a>
                            </sec:authorize></td>
                            <td><sec:authorize access="
                                                   hasRole('ROLE_ADMINISTRATOR') or
                                                   hasRole('ROLE_DATA_CLERK') or
                                                   hasRole('ROLE_M_AND_E_OFFICER') or
                                                   hasRole('ROLE_HOD_M_AND_E') or
                                                   hasRole('ROLE_ZM') or
                                                   hasRole('ROLE_ZI')
                                                   ">
                                <a href="${page}/report/patient/index.htm">Client Detailed Report</a>
                            </sec:authorize></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</form:form>
<%@include file="./template/footer.jspf"%>
