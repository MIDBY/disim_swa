package it.univaq.example.webshop.model2;

import java.util.UUID;

/**
 *
 * @author Giuseppe
 */
public class Prodotto {

    private String codice;
    private String descrizione;
    private int quantita;
    private String unita;
    private float prezzoUnitario;
    private float prezzoTotale;
    private byte iva;

    public Prodotto() {
        codice = "";
        descrizione = "";
        quantita = 0;
        unita = "N";
        prezzoUnitario = 0;
        prezzoTotale = 0;
        iva = 0;
    }

    /**
     * @return the codice
     */
    public String getCodice() {
        return codice;
    }

    /**
     * @param codice the codice to set
     */
    public void setCodice(String codice) {
        this.codice = codice;
    }

    /**
     * @return the descrizione
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * @param descrizione the descrizione to set
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * @return the quantita
     */
    public int getQuantita() {
        return quantita;
    }

    /**
     * @param quantita the quantita to set
     */
    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    /**
     * @return the unita
     */
    public String getUnita() {
        return unita;
    }

    /**
     * @param unita the unita to set
     */
    public void setUnita(String unita) {
        this.unita = unita;
    }

    /**
     * @return the prezzoUnitario
     */
    public float getPrezzoUnitario() {
        return prezzoUnitario;
    }

    /**
     * @param prezzoUnitario the prezzoUnitario to set
     */
    public void setPrezzoUnitario(float prezzoUnitario) {
        this.prezzoUnitario = prezzoUnitario;
    }

    /**
     * @return the prezzoTotale
     */
    public float getPrezzoTotale() {
        return prezzoTotale;
    }

    /**
     * @param prezzoTotale the prezzoTotale to set
     */
    public void setPrezzoTotale(float prezzoTotale) {
        this.prezzoTotale = prezzoTotale;
    }

    /**
     * @return the iva
     */
    public byte getIva() {
        return iva;
    }

    /**
     * @param iva the iva to set
     */
    public void setIva(byte iva) {
        this.iva = iva;
    }

    //metodo di prova
    public static Prodotto dummyProdotto() {
        Prodotto p = new Prodotto();
        p.setCodice(UUID.randomUUID().toString());
        p.setIva((byte) 4);
        p.setDescrizione("Prodotto inutile");
        p.setQuantita(10);
        p.setUnita("pz");
        p.setPrezzoUnitario((float) 12.0);
        p.setPrezzoTotale(p.getPrezzoUnitario() * p.getQuantita());

        return p;
    }
}