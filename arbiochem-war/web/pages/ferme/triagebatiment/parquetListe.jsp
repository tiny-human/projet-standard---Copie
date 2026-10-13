<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageRecherche"%>
<%@ page import="ferme.configuration.VParquetRecap"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Map"%>

<% try {
        VParquetRecap o = new VParquetRecap();
        o.setNomTable("V_PARQUET_RECAP");

        String[] listeCrt = {"ID_LOT"};
        String[] listeInt = {};

        String[] libEntete = {
            "NOM_PARQUET",
            "NOM_BATIMENT",
            "QUANTITE_AKOHO",
            "SEXE",
            "SANTE_MOYENNE",
            "QUALITE",
            "PRIX_UNITAIRE",
            "PRIX_TOTAL"
        };

        PageRecherche pr = new PageRecherche(o, request, listeCrt, listeInt, 20, libEntete, libEntete.length);
        pr.setTitre("Liste des parquets");
        pr.setUtilisateur((user.UserEJB) session.getValue("u"));
        pr.setLien((String) session.getValue("lien"));
        pr.setApres("ferme/triagebatiment/parquetListe.jsp");
        pr.setVisibleColMobile(true);
        pr.getFormu().getChamp("ID_LOT").setVisible(false);
        String[] libEnteteAffiche = {
            "Parquet",
            "Bâtiment",
            "Quantité Akoho",
            "Sexe",
            "Santé",
            "Qualité",
            "Prix unitaire",
            "Prix total"
        };
        pr.creerObjetPage(libEntete, null);
        Map<String, String> libelles = new HashMap<String, String>();
        for (int i = 0; i < libEntete.length; i++) {
            libelles.put(libEntete[i], libEnteteAffiche[i]);
        }
        pr.getTableau().setLibelleEnteteAAffiche(libelles);
%>

<div class="content-wrapper">
    <section class="content-header">
        <h1><%= pr.getTitre() %></h1>
    </section>
    <section class="content">
        <form action="<%=pr.getLien()%>?but=<%= pr.getApres() %>" method="post">
            <%= pr.getFormu().getHtmlEnsemble() %>
        </form>
        <%
            if (pr.getTableau().getHtml() != null) {
                out.println(pr.getTableau().getHtml());
                out.println(pr.getBasPage());
            } else {
        %>
            <center><h4>Aucune donnée trouvée</h4></center>
        <% } %>
    </section>
</div>

<% } catch (Exception e) {
   e.printStackTrace();
%>
<script language="JavaScript">
   alert('<%= e.getMessage() == null ? "Erreur lors du chargement de la liste" : e.getMessage().replace("'", "\\'") %>');
   history.back();
</script>
<% } %>
