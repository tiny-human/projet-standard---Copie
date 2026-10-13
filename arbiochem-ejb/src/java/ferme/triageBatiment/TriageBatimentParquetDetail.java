package ferme.triageBatiment;

import bean.ClassFille;
import java.sql.Connection;

public class TriageBatimentParquetDetail extends ClassFille {
    private String id;
    private String idmere;
    private String idbatiment;
    private String idparquet;
    private String idsexe;
    private String idqualite;
    private int qte;
    private int sante;

    public int getSante() {
        return sante;
    }

    public void setSante(int sante) {
        // Validation métier : valeur must be between 1 and 10 inclusive
        if (sante < 1 || sante > 10) {
            throw new IllegalArgumentException("La santé doit être comprise entre 1 et 10");
        }
        this.sante = sante;
    }

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

    public String getIdsexe() {
        return idsexe;
    }

    public void setIdsexe(String idsexe) {
        this.idsexe = idsexe;
    }

    public String getIdqualite() {
        return idqualite;
    }

    public void setIdqualite(String idqualite) {
        this.idqualite = idqualite;
    }

    public int getQte() {
        return qte;
    }

    public void setQte(int qte) {
        this.qte = qte;
    }


    @Override
    public String getNomClasseMere() {
        return "ferme.triageBatiment.TriageBatimentParquet";
    }

    @Override
    public String getLiaisonMere() {
        return "idmere";
    }

    public TriageBatimentParquetDetail() throws Exception {
        this.setNomTable("TRIAGEBATIMENTPARQUETDETAIL");
        this.setNomClasseMere("ferme.triageBatiment.TriageBatimentParquet");
        this.setLiaisonMere("idmere");
    }

    @Override
    public void construirePK(Connection c) throws Exception {
        this.preparePk("TBD","getseq_triagebatparquetdetail");
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

