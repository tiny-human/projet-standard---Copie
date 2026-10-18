package ferme.couvoir;

import bean.ClassFille;
import java.sql.Connection;
import java.sql.Date;
import bean.CGenUtil;
import bean.ClassMAPTable;
import ferme.configuration.Souche;
import ferme.lot.Lot;
import java.util.Calendar;

public class MiseEnIncubateurDetail extends ClassFille {
    private String id;
    private String idmere;
    private String idlot;
    private String idbatiment;
    private String idparquet;
    private String heuredemarrage;
    private String heurefin;
    private Date dateponte;
    private double qte;
    private double poidsmoyen;
    private Date dateeclosionprevue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdmere() {
        return idmere;
    }

    public void setIdmere(String idmere) {
        this.idmere = idmere;
    }

    public String getIdlot() {
        return idlot;
    }

    public void setIdlot(String idlot) {
        this.idlot = idlot;
    }

    public String getIdbatiment() {
        return idbatiment;
    }

    public void setIdbatiment(String idbatiment) {
        this.idbatiment = idbatiment;
    }

    public String getIdparquet() {
        return idparquet;
    }

    public void setIdparquet(String idparquet) {
        this.idparquet = idparquet;
    }

    public String getHeuredemarrage() {
        return heuredemarrage;
    }

    public void setHeuredemarrage(String heuredemarrage) {
        this.heuredemarrage = heuredemarrage;
    }

    public String getHeurefin() {
        return heurefin;
    }

    public void setHeurefin(String heurefin) {
        this.heurefin = heurefin;
    }

    public Date getDateponte() {
        return dateponte;
    }

    public void setDateponte(Date dateponte) {
        this.dateponte = dateponte;
    }

    public double getQte() {
        return qte;
    }

    public void setQte(double qte) {
        this.qte = qte;
    }

    public double getPoidsmoyen() {
        return poidsmoyen;
    }

    public void setPoidsmoyen(double poidsmoyen) {
        this.poidsmoyen = poidsmoyen;
    }

    public Date getDateeclosionprevue() {
        return dateeclosionprevue;
    }

    public void setDateeclosionprevue(Date dateeclosionprevue) {
        this.dateeclosionprevue = dateeclosionprevue;
    }

    @Override
    public ClassMAPTable createObject(String u, Connection c) throws Exception {
        calculerDateEclosionPrevue(c);
        return super.createObject(u, c);
    }

    private void calculerDateEclosionPrevue(Connection c) throws Exception {
        if (idmere == null || idmere.trim().isEmpty()) {
            throw new Exception(
                    "La mise en incubateur est obligatoire.");
        }

        if (idlot == null || idlot.trim().isEmpty()) {
            throw new Exception(
                    "Le lot est obligatoire.");
        }

        MiseEnIncubateur[] mises = (MiseEnIncubateur[]) CGenUtil.rechercher(
                new MiseEnIncubateur(),
                null,
                null,
                c,
                " AND ID = '" + idmere + "'");

        if (mises.length == 0 || mises[0].getDatemiseenmachine() == null) {
            throw new Exception(
                    "La date de mise en machine est obligatoire.");
        }

        Lot[] lots = (Lot[]) CGenUtil.rechercher(
                new Lot(),
                null,
                null,
                c,
                " AND ID = '" + idlot + "'");

        if (lots.length == 0) {
            throw new Exception(
                    "Le lot sélectionné est introuvable.");
        }

        String idSouche = lots[0].getIdSouche();

        if (idSouche == null || idSouche.trim().isEmpty()) {
            throw new Exception(
                    "Le lot sélectionné n'a pas de souche associée.");
        }

        Souche[] souches = (Souche[]) CGenUtil.rechercher(
                new Souche(),
                null,
                null,
                c,
                " AND ID = '" + idSouche + "'");

        if (souches.length == 0) {
            throw new Exception(
                    "La souche du lot est introuvable.");
        }

        int incubation = souches[0].getIncubation();

        if (incubation <= 0) {
            throw new Exception(
                    "La durée d'incubation doit être supérieure à zéro.");
        }

        Calendar calendrier = Calendar.getInstance();
        calendrier.setTime(
                mises[0].getDatemiseenmachine());
        calendrier.add(
                Calendar.DAY_OF_MONTH,
                incubation);

        dateeclosionprevue = new Date(
                calendrier.getTimeInMillis());
    }

    @Override
    public String getNomClasseMere() {
        return "ferme.couvoir.MiseEnIncubateur";
    }

    @Override
    public String getLiaisonMere() {
        return "idmere";
    }

    public MiseEnIncubateurDetail() throws Exception {
        this.setNomTable("MISEENINCUBATEURDETAIL");
        this.setNomClasseMere("ferme.couvoir.MiseEnIncubateur");
        this.setLiaisonMere("idmere");
    }

    @Override
    public void construirePK(Connection c) throws Exception {
        this.preparePk("MEID", "getseq_miseenincubateurdetail");
        this.setId(makePK(c));
    }

    @Override
    public String getTuppleID() {
        return id;
    }

    @Override
    public String getAttributIDName() {
        return "id";
    }

}
