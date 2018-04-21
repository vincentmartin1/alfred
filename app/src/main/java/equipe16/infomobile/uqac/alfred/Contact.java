package equipe16.infomobile.uqac.alfred;

public class Contact {
    private String displayName;
    private String numero;
    private String mail;

    public Contact(String displayName, String numero, String mail) {
        this.displayName = displayName;
        this.numero = numero;
        this.mail = mail;
    }

    public String getDisplayName() { return displayName; }

    public String getNumero() {
        return numero;
    }

    public String getMail() {
        return mail;
    }
}
