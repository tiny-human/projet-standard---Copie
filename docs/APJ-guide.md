# Guide pratique APJ

## 1. Objet du guide

Ce document décrit l'utilisation d'APJ telle qu'elle existe dans ce projet :

- modèles Java `ClassMAPTable` ;
- pages JSP `PageRecherche`, `PageConsulte`, `PageInsert` et `PageUpdate` ;
- formulaires et tableaux générés par APJ ;
- filtres, tri, pagination, liens et actions ;
- vues SQL et classes Java associées ;
- diagnostic des erreurs de réflexion et de déploiement.

Ce n'est pas la documentation officielle d'APJ. Les JARs APJ disponibles dans
`build-file/lib` sont utilisés comme référence lorsque le comportement n'est
pas évident dans les JSP.

---

## 2. Architecture générale

Une page APJ relie quatre couches :

```text
Base Oracle
    ↓
Classe Java ClassMAPTable
    ↓
Page APJ (PageRecherche/PageConsulte/PageInsert/PageUpdate)
    ↓
JSP + module.jsp + HTML généré
```

### 2.1 Rôle de chaque couche

| Couche | Rôle |
|---|---|
| Table ou vue SQL | Stocke ou expose les colonnes |
| `ClassMAPTable` | Décrit les champs, la table et l'identifiant |
| Page APJ | Prépare les données, formulaires, liens et pagination |
| JSP | Configure APJ et place le HTML dans la page |
| `module.jsp` | Route `but=...` vers le JSP demandé |

Le routage de l'application suit généralement ce modèle :

```text
/async-standard/module.jsp?but=ferme/triagebatiment/parquetListe.jsp&ID_LOT=LOT000008
```

Le chemin de page appartient à `but`. Les autres paramètres utilisent `&`.
Il ne faut pas écrire un deuxième `?` avant `ID_LOT`.

---

## 3. Trouver les classes et les JARs APJ

Les JARs utilisés par le projet sont notamment :

```text
build-file/lib/apj-core.jar
build-file/lib/apj-core2.jar
```

Pour consulter une API publique :

```bash
javap -classpath 'build-file/lib/apj-core.jar:build-file/lib/apj-core2.jar' \
  -p affichage.PageRecherche
```

Pour examiner l'implémentation bytecode :

```bash
javap -classpath 'build-file/lib/apj-core.jar:build-file/lib/apj-core2.jar' \
  -c -p affichage.PageRecherche
```

Cette méthode est utile lorsque le code source APJ n'est pas disponible.

---

## 4. Créer un modèle Java APJ

### 4.1 Modèle de table classique

Exemple minimal :

```java
package ferme.exemple;

import bean.ClassMAPTable;
import java.sql.Connection;

public class Exemple extends ClassMAPTable {
    private String id;
    private String libelle;
    private double montant;

    public Exemple() throws Exception {
        setNomTable("EXEMPLE");
    }

    @Override
    public void construirePK(Connection c) throws Exception {
        preparePk("EX", "getseq_exemple");
        setId(makePK(c));
    }

    @Override
    public String getTuppleID() {
        return id;
    }

    @Override
    public String getAttributIDName() {
        return "id";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }
}
```

### 4.2 Méthodes indispensables

| Méthode | Utilité |
|---|---|
| `setNomTable` | Table ou vue utilisée par APJ |
| `getTuppleID` | Valeur de l'identifiant courant |
| `getAttributIDName` | Nom Java/SQL de l'identifiant |
| `construirePK` | Génération de clé pour une insertion |
| getters/setters | Lecture et écriture par réflexion |

`ClassMAPTable` fournit aussi des opérations comme `insertToTable`,
`updateToTable`, `deleteToTable`, `getById`, `rechercherPage` et
`setValChamp`.

### 4.3 Modèle d'une vue SQL

Pour une vue en lecture seule, le modèle suit généralement le même contrat,
mais `construirePK` ne sera pas utilisé pour une insertion réelle.

```java
public class VExemple extends ClassMAPTable {
    private String ID;
    private String LIBELLE;
    private double MONTANT;

    public VExemple() throws Exception {
        setNomTable("V_EXEMPLE");
    }

    @Override
    public void construirePK(Connection c) throws Exception {
        preparePk("VEX", "GETSEQ_V_EXEMPLE");
        setID(makePK(c));
    }

    @Override
    public String getTuppleID() {
        return ID;
    }

    @Override
    public String getAttributIDName() {
        return "ID";
    }
}
```

### 4.4 Règle importante sur les noms

APJ compare les champs Java déclarés aux noms des colonnes JDBC. Dans la
version observée ici, la comparaison est insensible à la casse mais ne retire
pas les underscores.

Ainsi :

```text
NOM_PARQUET  ↔  nom_parquet
NOM_PARQUET  ✗  nomParquet
```

Pour une vue contenant des colonnes `ID_LOT`, `NOM_PARQUET`, etc., utiliser
des champs Java correspondants :

```java
private String ID_LOT;
private String NOM_PARQUET;
```

et les getters :

```java
public String getID_LOT() { return ID_LOT; }
public void setID_LOT(String value) { ID_LOT = value; }
```

Une classe fille vide peut également poser problème : certaines parties APJ
utilisent `getDeclaredFields()` sur la classe concrète. Pour une vue, utiliser
directement la classe qui déclare réellement les champs.

### 4.5 Types numériques

Pour une colonne numérique Oracle, utiliser de préférence un type primitif
(`double`, `int`) si APJ doit toujours recevoir une valeur. Utiliser un objet
(`Double`, `Integer`) si `NULL` doit être distingué de zéro, en vérifiant que
la version APJ utilisée sait mapper ce type.

---

## 5. Créer une page de recherche/liste

### 5.1 Exemple standard

```jsp
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageRecherche" %>
<%@ page import="exemple.Exemple" %>

<%
try {
    Exemple objet = new Exemple();
    objet.setNomTable("EXEMPLE");

    String[] listeCrt = {"libelle", "montant"};
    String[] listeInt = {"montant"};
    String[] libEntete = {"id", "libelle", "montant"};

    PageRecherche pr = new PageRecherche(
        objet,
        request,
        listeCrt,
        listeInt,
        20,
        libEntete,
        libEntete.length
    );

    pr.setTitre("Liste des exemples");
    pr.setUtilisateur((user.UserEJB) session.getValue("u"));
    pr.setLien((String) session.getValue("lien"));
    pr.setApres("exemple/exemple-liste.jsp");

    pr.getFormu().getChamp("libelle").setLibelle("Libellé");
    pr.getFormu().getChamp("montant1").setLibelle("Montant minimum");
    pr.getFormu().getChamp("montant2").setLibelle("Montant maximum");

    String[] colSomme = {"montant"};
    pr.creerObjetPage(libEntete, colSomme);
%>

<div class="content-wrapper">
    <section class="content-header">
        <h1><%= pr.getTitre() %></h1>
    </section>
    <section class="content">
        <form action="<%= pr.getLien() %>?but=<%= pr.getApres() %>" method="post">
            <%= pr.getFormu().getHtmlEnsemble() %>
        </form>
        <%= pr.getTableauRecap().getHtml() %>
        <%= pr.getTableau().getHtml() %>
        <%= pr.getBasPage() %>
    </section>
</div>

<%
} catch (Exception e) {
    e.printStackTrace();
%>
<script>
    alert('Erreur lors du chargement de la liste');
    history.back();
</script>
<%
}
%>
```

### 5.2 Paramètres du constructeur

```java
new PageRecherche(
    objet,
    request,
    listeCrt,
    listeInt,
    nombreParPage,
    colonnesAffichees,
    nombreColonnes
)
```

| Paramètre | Signification |
|---|---|
| `objet` | Modèle `ClassMAPTable` |
| `request` | Requête HTTP courante |
| `listeCrt` | Champs recherchables |
| `listeInt` | Champs numériques ou intervalles |
| `nombreParPage` | Nombre de lignes par page |
| `colonnesAffichees` | Champs affichés |
| `nombreColonnes` | Taille du tableau d'affichage |

### 5.3 `listeCrt` et filtres

`listeCrt` déclare les champs que le formulaire peut filtrer :

```java
String[] listeCrt = {"ID_LOT", "NOM_PARQUET", "SEXE"};
```

APJ lit ensuite les paramètres HTTP correspondants et construit une clause
de recherche. Pour un filtre texte, le comportement observé produit
généralement une condition de type :

```sql
upper(NOM_PARQUET) like upper('%parquet%')
```

Pour un critère numérique placé dans `listeInt`, APJ utilise les bornes
générées par le formulaire, souvent avec les suffixes `1` et `2` :

```text
montant1 = borne minimale
montant2 = borne maximale
```

Pour masquer un critère tout en le conservant comme filtre transmis dans
l'URL :

```java
pr.getFormu().getChamp("ID_LOT").setVisible(false);
```

Le filtre de la page des parquets fonctionne ainsi :

```text
bouton fiche
  → &ID_LOT=LOT000008
  → listeCrt = {"ID_LOT"}
  → formulaire APJ lit ID_LOT
  → SQL : ... where ... and upper(ID_LOT) like ...
```

### 5.4 `listeInt`

Exemple :

```java
String[] listeCrt = {"daty", "quantite"};
String[] listeInt = {"daty", "quantite"};
```

Les champs numériques et dates sont affichés avec des bornes lorsque APJ
reconnaît le type. Vérifier les noms générés dans le HTML ou dans les logs
SQL lorsque le filtre ne répond pas comme prévu.

### 5.5 `setAWhere` et `setMultipleAwhere`

Ces méthodes permettent d'ajouter une condition SQL :

```java
pr.setAWhere(" AND ETAT >= 1");
```

À utiliser uniquement avec des valeurs contrôlées. Ne jamais concaténer
directement une valeur utilisateur non validée dans `setAWhere`.

---

## 6. Tri par colonne

### 6.1 Tri natif APJ

APJ génère des liens de tri avec des paramètres comme :

```text
triCol=yes
newcol=NOM_PARQUET
colonne=NOM_PARQUET
ordre=-
```

Le tableau et la pagination utilisent ces paramètres pour refaire la requête
avec un ordre serveur. C'est préférable à un tri JavaScript local, car le tri
reste correct lorsque les lignes sont réparties sur plusieurs pages.

### 6.2 Ce qui se passe au clic

Schéma général :

```text
clic sur l'en-tête
  → newcol = colonne sélectionnée
  → triCol = yes
  → APJ détermine l'ordre suivant
  → PageRecherche relit la requête
  → SQL est exécuté avec ORDER BY
  → tableau et pagination sont régénérés
```

Le tri texte est alphabétique. Les nombres sont triés comme des nombres si
APJ connaît le type JDBC de la colonne.

### 6.3 Conserver le filtre pendant le tri

Le lien de tri doit conserver les paramètres de filtre :

```text
...?but=...&ID_LOT=LOT000008&triCol=yes&newcol=NOM_PARQUET
```

Sinon, le clic sur un en-tête peut perdre le lot courant.

### 6.4 Tri imposé par la page

Pour un ordre fixe initial, `PageRecherche` expose aussi :

```java
pr.setOrdre("DATY DESC");
```

Ne pas confondre cette méthode avec le tri interactif des en-têtes.

---

## 7. Pagination et colonnes

Toujours afficher la pagination après le tableau :

```jsp
<%= pr.getBasPage() %>
```

Les méthodes utiles de `PageRecherche` comprennent :

```java
pr.setNpp(20);
pr.getNpp();
pr.setNumPage(1);
pr.getNumPage();
pr.getNombrePage();
pr.getBasPage();
```

`npp` signifie généralement « nombre par page ». Une taille trop faible donne
plus de pages ; une taille trop grande peut ralentir la page.

---

## 8. Liens dans un tableau

Pour rendre une colonne cliquable :

```java
String[] lienTableau = {
    pr.getLien() + "?but=exemple/exemple-fiche.jsp"
};
String[] colonneLien = {"id"};
String[] attributLien = {"id"};

pr.getTableau().setLien(lienTableau);
pr.getTableau().setColonneLien(colonneLien);
pr.getTableau().setAttLien(attributLien);
```

Pour les détails d'une classe mère :

```java
pr.getTableau().setLienFille(
    "ferme/triagebatiment/inc/triagebatimentparquet-det.jsp&id="
);
```

Vérifier le format attendu par la page cible : certains liens APJ ajoutent
déjà `?but=`, d'autres attendent une chaîne commençant par `&id=`.

---

## 9. Consulter une fiche avec `PageConsulte`

```jsp
<%
    user.UserEJB u = (user.UserEJB) session.getValue("u");
    String lien = (String) session.getValue("lien");

    Exemple objet = new Exemple();
    PageConsulte pc = new PageConsulte(objet, request, u);
    pc.setTitre("Fiche exemple");

    pc.getChampByName("libelle").setLibelle("Libellé");
    pc.getChampByName("montant").setLibelle("Montant");

    String[] ordre = {"id", "libelle", "montant"};
    pc.setOrdre(ordre);
%>

<%= pc.getHtml() %>
```

Le `PageConsulte` lit généralement l'identifiant depuis `request`, charge
l'objet et produit une présentation en lecture seule.

---

## 10. Créer un formulaire avec `PageInsert`

```jsp
<%
    Exemple objet = new Exemple();
    PageInsert pi = new PageInsert(
        objet,
        request,
        (user.UserEJB) session.getValue("u")
    );

    pi.getFormu().getChamp("libelle").setLibelle("Libellé");
    pi.getFormu().getChamp("montant").setLibelle("Montant");
    pi.getFormu().getChamp("montant").setDefaut("0");
%>

<%= pi.getFormu().getHtmlEnsemble() %>
```

Méthodes fréquentes :

```java
pi.getObjectAvecValeur();
pi.getFormu().getChamp("nom");
pi.getFormu().getChamp("nom").setDefaut("Valeur");
pi.getFormu().getChamp("nom").setVisible(false);
pi.getFormu().getChamp("nom").setAutre("readonly");
pi.getFormu().getChamp("nom").setType("password");
pi.getFormu().getChamp("nom").setLibelle("Libellé");
```

La sauvegarde est généralement effectuée par une page d'action comme
`apresTarif.jsp`, selon les conventions du projet.

---

## 11. Modifier avec `PageUpdate`

```jsp
<%
    Exemple objet = new Exemple();
    PageUpdate pu = new PageUpdate(
        objet,
        request,
        (user.UserEJB) session.getValue("u")
    );

    pu.getFormu().getChamp("id").setAutre("readonly");
    pu.getFormu().getChamp("libelle").setLibelle("Libellé");
%>

<%= pu.getFormu().getHtmlEnsemble() %>
```

Le paramètre `id` ou le nom de clé attendu doit être présent dans l'URL :

```text
...?but=exemple/exemple-modif.jsp&id=EX000001
```

---

## 12. Champs et formulaires

Méthodes fréquemment utilisées sur un champ APJ :

```java
Champ champ = pr.getFormu().getChamp("nom");

champ.setLibelle("Nom affiché");
champ.setVisible(false);
champ.setDefaut("valeur");
champ.setValeur("valeur");
champ.setAutre("readonly");
champ.setType("password");
champ.setLien(url, "id=");
champ.setPageAppelComplete(classe, champRetour, table);
```

Pour les dates avec bornes :

```java
pr.getFormu().getChamp("daty1").setLibelle("Date min");
pr.getFormu().getChamp("daty2").setLibelle("Date max");
```

Pour réordonner les champs :

```java
String[] ordre = {"id", "libelle", "montant"};
pr.getFormu().setOrdre(ordre);
```

---

## 13. Classes mères et filles

Une classe mère peut déclarer une classe fille :

```java
public TriageBatimentParquet() throws Exception {
    setNomTable("TRIAGEBATIMENTPARQUET");
    setNomClasseFille(
        "ferme.triageBatiment.TriageBatimentParquetDetail"
    );
    setLiaisonFille("idmere");
}
```

Cela permet à APJ de représenter les détails liés à une fiche maître.

Dans une page de liste :

```java
pr.getTableau().setLienFille(
    "ferme/triagebatiment/inc/triagebatimentparquet-det.jsp&id="
);
```

Utiliser `ClassMere` uniquement lorsque la relation maître/détail est réelle.
Pour une vue récapitulative indépendante, préférer `ClassMAPTable`.

---

## 14. Vues SQL et récapitulatifs

Une vue utilisée par APJ doit respecter trois contrats :

1. toutes les colonnes exposées doivent être connues de la classe Java ;
2. les noms de champs Java doivent correspondre aux noms JDBC ;
3. les types JDBC doivent être compatibles avec les types Java.

Exemple :

```sql
CREATE OR REPLACE VIEW V_EXEMPLE AS
SELECT
    ID AS ID,
    LIBELLE AS LIBELLE,
    SUM(QTE) AS QUANTITE
FROM DETAIL
GROUP BY ID, LIBELLE;
```

La classe doit déclarer `ID`, `LIBELLE` et `QUANTITE`. Une colonne ajoutée
dans la vue doit être ajoutée dans la classe, et inversement.

Pour diagnostiquer :

```sql
SELECT column_id, column_name, data_type, data_precision, data_scale
FROM all_tab_columns
WHERE owner = 'AKOHO'
  AND table_name = 'V_EXEMPLE'
ORDER BY column_id;
```

Tester également la requête directement :

```sql
SELECT *
FROM AKOHO.V_EXEMPLE
WHERE 1 < 2;
```

`NVL(colonne, 0)` transforme `NULL` en zéro. Cela peut être correct pour
l'affichage, mais masque la différence entre « inconnu » et « zéro réel ».

---

## 15. Affichage des libellés de colonnes

Dans les cas ordinaires :

```java
String[] libEnteteAffiche = {
    "Id",
    "Libellé",
    "Montant"
};
pr.getTableau().setLibelleAffiche(libEnteteAffiche);
```

Le nombre de libellés doit correspondre aux colonnes réellement transmises à
`creerObjetPage`.

Certaines versions/configurations APJ utilisent aussi une map :

```java
Map<String, String> libelles = new HashMap<String, String>();
libelles.put("NOM_PARQUET", "Parquet");
libelles.put("NOM_BATIMENT", "Bâtiment");
pr.getTableau().setLibelleEnteteAAffiche(libelles);
```

Si les données apparaissent mais pas les en-têtes, vérifier dans cet ordre :

1. le tableau contient-il un `<thead>` ?
2. `libEntete` contient-il les mêmes noms que les champs Java ?
3. le nombre de libellés est-il correct ?
4. le HTML APJ est-il généré avant ou après la configuration ?
5. un CSS ou un mode mobile masque-t-il les colonnes ?
6. la page affiche-t-elle réellement `pr.getTableau().getHtml()` ?

---

## 16. Filtre, tri et pagination : exemple complet

Pour une vue `V_PARQUET_RECAP` filtrée par lot :

```java
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
```

Le bouton de la fiche transmet :

```jsp
...?but=ferme/triagebatiment/parquetListe.jsp&ID_LOT=<%= base.getIdlot() %>
```

La page de recherche conserve ce critère :

```java
pr.getFormu().getChamp("ID_LOT").setVisible(false);
```

Le cacher dans le formulaire ne supprime pas le filtre ; cela le rend
seulement invisible à l'utilisateur.

La requête observée dans les logs ressemble à :

```sql
select *
from V_PARQUET_RECAP
where 1 < 2
  and upper(ID_LOT) like upper('%LOT000008%')
```

Le `1 < 2` est une construction interne APJ utilisée pendant la préparation
ou la construction du tableau. Il ne signifie pas que la vue est vide.

---

## 17. Navigation et actions

Le modèle standard d'un footer de fiche est :

```jsp
<a href="<%= lien %>?but=apresTarif.jsp&id=<%= id %>
    &acte=valider&bute=<%= pageActuel %>&classe=<%= classe %>">
    Valider
</a>
```

Toujours vérifier :

- `but` pointe vers une page existante ;
- les paramètres supplémentaires utilisent `&` ;
- `bute` est correctement encodé ;
- le lien n'est pas placé dans une condition d'état trop restrictive ;
- les IDs transmis correspondent au modèle cible.

Un identifiant de triage maître n'est pas automatiquement l'identifiant du
lot, et un identifiant de lot n'est pas automatiquement l'identifiant d'un
parquet détail.

---

## 18. Gestion des erreurs JSP

Pendant le développement, conserver la trace serveur :

```jsp
} catch (Exception e) {
    e.printStackTrace();
%>
<script>
    alert('<%= e.getMessage() == null
        ? "Erreur"
        : e.getMessage().replace("'", "\\'") %>');
    history.back();
</script>
<%
}
%>
```

Ne pas afficher silencieusement un tableau vide lorsqu'une exception a
empêché le chargement. Une erreur de mapping doit être visible dans les logs.

---

## 19. Diagnostic systématique

### 19.1 JSP ne compile pas

Vérifier :

- chaque scriptlet Java est fermé par `%>` ;
- les variables déclarées dans un `try` sont utilisées dans le même scope ;
- les balises HTML ne sont pas placées dans un scriptlet Java non fermé ;
- les imports JSP sont valides.

### 19.2 `NullPointerException` dans `CGenUtil.transformeDataReader`

Vérifier :

1. les colonnes de la vue avec `all_tab_columns` ;
2. les champs déclarés dans la classe Java ;
3. les underscores et la casse des noms ;
4. l'absence de classe fille vide utilisée comme modèle ;
5. les types Oracle (`NUMBER`, `VARCHAR2`, `DATE`) ;
6. le JAR réellement déployé ;
7. le cache/mapping APJ après changement de vue.

Le message :

```text
select * from V_EXEMPLE where 1<2
```

indique souvent un échec pendant la préparation des métadonnées, avant le
traitement des lignes réelles.

### 19.3 Données SQL correctes mais colonnes absentes

Vérifier si le problème est :

- un en-tête CSS masqué ;
- une largeur de tableau ;
- un mode mobile APJ ;
- une colonne cachée dans `TableauRecherche` ;
- un mauvais `libEntete` ;
- un HTML APJ affiché partiellement.

Inspecter le HTML rendu dans le navigateur plutôt que seulement les valeurs
visibles.

### 19.4 Filtre ignoré

Vérifier :

- le nom exact du paramètre (`ID_LOT` contre `idLot`) ;
- le nom exact de `listeCrt` ;
- la présence du paramètre dans l'URL ;
- le champ dans la vue SQL ;
- la requête générée dans `server.log`.

### 19.5 Déploiement non pris en compte

Le projet contient plusieurs copies :

```text
arbiochem-war/web/pages/...
build-file/arbiochem_war/pages/...
standalone/deployments/async-standard.war/pages/...
```

Utiliser :

```bash
ant -f build.xml -q deploy
```

Puis vérifier que le JSP déployé contient réellement la modification.

---

## 20. Checklist pour une nouvelle page

### Modèle

- [ ] La classe étend le bon type (`ClassMAPTable`, `ClassMere`, etc.).
- [ ] `setNomTable` utilise le bon nom SQL.
- [ ] L'identifiant est défini.
- [ ] Les champs Java correspondent aux colonnes SQL.
- [ ] Tous les getters/setters existent.
- [ ] Le constructeur ne dépend pas d'une donnée absente.

### Liste

- [ ] `listeCrt` contient les critères voulus.
- [ ] `listeInt` contient les champs numériques/dates concernés.
- [ ] `libEntete` contient les colonnes à afficher.
- [ ] `libEnteteAffiche` a la même longueur.
- [ ] `setTitre`, `setUtilisateur`, `setLien` sont configurés.
- [ ] `setApres` est configuré.
- [ ] `creerObjetPage` est appelé une seule fois au bon endroit.
- [ ] `getTableau().getHtml()` et `getBasPage()` sont affichés.

### Navigation

- [ ] `but` contient le chemin JSP.
- [ ] Les paramètres supplémentaires utilisent `&`.
- [ ] Les liens transmettent le bon type d'identifiant.
- [ ] Les filtres sont conservés dans les liens de tri/pagination.

### Base

- [ ] La vue/table existe dans le schéma attendu.
- [ ] Les colonnes ont les bons noms.
- [ ] Les agrégats sont présents dans `GROUP BY`.
- [ ] Les `NULL` et les zéros sont intentionnels.
- [ ] La requête SQL directe fonctionne.

### Validation

- [ ] `ant -f build.xml -q deploy` réussit.
- [ ] Le JSP déployé est la bonne version.
- [ ] Les logs SQL contiennent le filtre attendu.
- [ ] La page fonctionne avec zéro, une et plusieurs lignes.
- [ ] Le tri conserve les filtres.
- [ ] La pagination conserve les filtres.

---

## 21. Choisir l'outil adapté

| Besoin | Outil APJ |
|---|---|
| Liste filtrable/paginée | `PageRecherche` |
| Fiche en lecture seule | `PageConsulte` |
| Création | `PageInsert` |
| Modification | `PageUpdate` |
| Maître/détails | `ClassMere` + `setNomClasseFille` |
| Vue SQL en lecture | `ClassMAPTable` |
| En-têtes et liens | `TableauRecherche` |
| Champs et critères | `Formulaire` / `Champ` |
| Accès modèle et persistance | `ClassMAPTable` |

---

## 22. Limites et précautions

- APJ dépend fortement de la réflexion et des conventions de nommage.
- Les erreurs internes de mapping peuvent être des `NullPointerException`
  peu explicites.
- Les vues SQL doivent rester synchronisées avec les classes Java.
- Les modifications de JAR ne sont pas recommandées sans source et tests.
- Les clauses SQL ajoutées manuellement doivent être contrôlées.
- Une classe de vue ne doit pas être utilisée pour insérer ou modifier sans
  vérifier la vraie clé et les contraintes de la base.
- Une clé d'affichage comme `ID_PARQUET` peut ne pas être unique si la vue
  regroupe par parquet, bâtiment, sexe et qualité.

---

## 23. Résumé opérationnel

Pour créer rapidement une page APJ :

```text
1. Créer la table/vue SQL.
2. Créer une classe ClassMAPTable avec les champs exacts.
3. Implémenter l'identifiant.
4. Définir listeCrt, listeInt et libEntete.
5. Construire PageRecherche/PageConsulte/PageInsert/PageUpdate.
6. Configurer les libellés et les liens.
7. Afficher le HTML APJ et la pagination.
8. Vérifier la requête SQL dans les logs.
9. Tester filtre, tri, pagination et URL.
10. Compiler et déployer.
```

Le meilleur point de départ local reste une page existante qui ressemble au
besoin, par exemple :

- une liste avec `PageRecherche` ;
- une fiche avec `PageConsulte` ;
- une saisie avec `PageInsert` ;
- une modification avec `PageUpdate`.

Copier sa structure, puis modifier progressivement le modèle, les critères,
les colonnes et les liens plutôt que de repartir d'une JSP vide.
