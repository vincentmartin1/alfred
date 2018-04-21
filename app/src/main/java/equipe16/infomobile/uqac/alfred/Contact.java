package equipe16.infomobile.uqac.alfred;

public class Contact {
    private String displayName;
    private String numero;

    public Contact(String displayName, String numero) {
        this.displayName = displayName;
        this.numero = numero;
    }

    public String getDisplayName() { return displayName; }

    public String getNumero() {
        return numero;
    }
}
