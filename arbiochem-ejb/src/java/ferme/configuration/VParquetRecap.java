package ferme.configuration;

import java.sql.Connection;
import bean.ClassMAPTable;

public class VParquetRecap extends ClassMAPTable {
    private String ID_PARQUET;
    private String ID_BATIMENT;
    private String ID_LOT;
    private String NOM_BATIMENT;
    private String NOM_PARQUET;
    private double QUANTITE_AKOHO;
    private String SEXE;
    private double SANTE_MOYENNE;
    private String QUALITE;
    private double PRIX_UNITAIRE;
    private double PRIX_TOTAL;

    public VParquetRecap() throws Exception {
        super.setNomTable("V_PARQUET_RECAP");   // nom exact de la vue
    }

    @Override
    public void construirePK(Connection c) throws Exception {
        this.preparePk("VPK", "GETSEQ_V_PARQUET_RECAP"); // séquence factice ou réelle
        this.setID_PARQUET(this.makePK(c));
    }

    @Override
    public String getAttributIDName() {
        return "ID_PARQUET";
    }

    @Override
    public String getTuppleID() {
        return ID_PARQUET;
    }

    public String getID_PARQUET() { return ID_PARQUET; }
    public void setID_PARQUET(String v) { this.ID_PARQUET = v; }
    public String getID_BATIMENT() { return ID_BATIMENT; }
    public void setID_BATIMENT(String v) { this.ID_BATIMENT = v; }
    public String getID_LOT() { return ID_LOT; }
    public void setID_LOT(String v) { this.ID_LOT = v; }
    public String getNOM_BATIMENT() { return NOM_BATIMENT; }
    public void setNOM_BATIMENT(String v) { this.NOM_BATIMENT = v; }
    public String getNOM_PARQUET() { return NOM_PARQUET; }
    public void setNOM_PARQUET(String v) { this.NOM_PARQUET = v; }
    public double getQUANTITE_AKOHO() { return QUANTITE_AKOHO; }
    public void setQUANTITE_AKOHO(double v) { this.QUANTITE_AKOHO = v; }
    public String getSEXE() { return SEXE; }
    public void setSEXE(String v) { this.SEXE = v; }
    public double getSANTE_MOYENNE() { return SANTE_MOYENNE; }
    public void setSANTE_MOYENNE(double v) { this.SANTE_MOYENNE = v; }
    public String getQUALITE() { return QUALITE; }
    public void setQUALITE(String v) { this.QUALITE = v; }
    public double getPRIX_UNITAIRE() { return PRIX_UNITAIRE; }
    public void setPRIX_UNITAIRE(double v) { this.PRIX_UNITAIRE = v; }
    public double getPRIX_TOTAL() { return PRIX_TOTAL; }
    public void setPRIX_TOTAL(double v) { this.PRIX_TOTAL = v; }
}
