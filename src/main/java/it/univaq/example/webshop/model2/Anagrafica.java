package it.univaq.example.webshop.model2;

/**
 *
 * @author Giuseppe
 */
public class Anagrafica {

    private String ragioneSociale;
    private String partitaIVA;
    private String via;
    private String citta;
    private String civico;

    public Anagrafica() {
        ragioneSociale = "";
        partitaIVA = "";
        via = "";
        citta = "";
        civico = "";
    }

    /**
     * @return the ragioneSociale
     */
    public String getRagioneSociale() {
        return ragioneSociale;
    }

    /**
     * @param ragioneSociale the ragioneSociale to set
     */
    public void setRagioneSociale(String ragioneSociale) {
        this.ragioneSociale = ragioneSociale;
    }

    /**
     * @return the partitaIVA
     */
    public String getPartitaIVA() {
        return partitaIVA;
    }

    /**
     * @param partitaIVA the partitaIVA to set
     */
    public void setPartitaIVA(String partitaIVA) {
        this.partitaIVA = partitaIVA;
    }

    /**
     * @return the via
     */
    public String getVia() {
        return via;
    }

    /**
     * @param via the via to set
     */
    public void setVia(String via) {
        this.via = via;
    }

    /**
     * @return the citta
     */
    public String getCitta() {
        return citta;
    }

    /**
     * @param citta the citta to set
     */
    public void setCitta(String citta) {
        this.citta = citta;
    }

    /**
     * @return the civico
     */
    public String getCivico() {
        return civico;
    }

    /**
     * @param civico the civico to set
     */
    public void setCivico(String civico) {
        this.civico = civico;
    }

}