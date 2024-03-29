package agents;

// Exemple de Bitxo
import java.util.Random;
import java.time.Instant;
import java.time.Duration;

public class Bitxo28 extends Agent {

    static final int PARET = 0;
    static final int BITXO = 1;
    static final int RES = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    Estat estat;
    Random r = new Random();
    int noMirar = 0;

    public Bitxo28(Agents pare) {
        super(pare, "123", "imatges/robotank1.gif");
    }

    @Override
    public void inicia() {
        // atributsAgents(v,w,dv,av,ll,es,hy)
        int cost = atributsAgent(6, 7, 600, 30, 23, 5, 5);
        System.out.println("Cost total:" + cost);

        // Inicialització de variables que utilitzaré al meu comportament
    }
    int contador = 0;
    Instant a = Instant.now();
    int girar = 0;
    @Override
    public void avaluaComportament() {
        estat = estatCombat();
        if (noMirar == 0) {
            mirar();
        } else {
            noMirar--;
        }
        camina();
    }

    private void camina() {

        if (hiHaParet(18)) {
            if (estat.enCollisio && (estat.distanciaVisors[CENTRAL] < 10 || estat.distanciaVisors[ESQUERRA] < 10 || estat.distanciaVisors[DRETA] < 10)) {
                atura();
                enrere();
                noMirar = 10;
            } else if (estat.objecteVisor[CENTRAL] == PARET) {
                if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
                    atura();
                    gira(20 + r.nextInt(20));
                    endavant();
                } else {
                    atura();
                    gira((20+r.nextInt(20))*-1);
                    endavant();
                }
                noMirar = 2;

            } else {
                if(girar != 0){
                    if(girar<0){
                        atura();
                        esquerra();
                        endavant();
                        girar++;
                    } else{
                        atura();
                        dreta();
                        endavant();
                    }
                } else if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
                    atura();
                    esquerra();
                    endavant();
                    girar++;
                } else {
                    atura();
                    dreta();
                    endavant();
                    girar--;
                }
            }
        } else if (hiHaParet(85)) {
            if (estat.distanciaVisors[CENTRAL] < 85) {
                if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
                    atura();
                    esquerra();
                    endavant();
                } else {
                    atura();
                    dreta();
                    endavant();
                }

            }
            
        } else {
            int aux = comidaEnem();
            switch (aux) {
                case 2:
                    atura();
                    dreta();
                    endavant();
                    break;
                case 3:
                    atura();
                    esquerra();
                    endavant();
                    break;
                default:
                    atura();
                    endavant();
                    break;
            }
        }
    }

    private int comidaEnem() {
        if (estat.veigAlgunRecurs) {
            for (int i = 0; i < estat.numObjectes; i++) {
                Objecte aux = estat.objectes[i];
                if (aux.agafaTipus() > 100 && aux.agafaTipus() != 100 + estat.id && aux.agafaDistancia() < 20) {
                    return aux.agafaSector();
                }
            }
        }
        return 0;
    }

    private void mirar() {
        Objecte fin = null;
        if (estat.veigAlgunRecurs || estat.veigAlgunEscut) {
            int distMin = 9999999;
            for (int i = 0; i < estat.numObjectes; i++) {
                Objecte aux = estat.objectes[i];
                if (aux.agafaTipus() == 100 + estat.id || aux.agafaTipus() == Estat.ESCUT) {
                    if (distMin > aux.agafaDistancia()) {
                        distMin = aux.agafaDistancia();
                        fin = aux;
                    }
                }
            }
            mira(fin);
        }
    }

    private boolean hiHaParet(int distancia) {

        return (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < distancia)
                || (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < distancia)
                || (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < distancia);
    }

}
