package agents;

// Exemple de Bitxo
import java.util.Random;

public class Bitxo27 extends Agent {

    static final int PARET = 0;
    static final int BITXO = 1;
    static final int RES = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    static final int A_ENRERE = 1;
    static final int A_ENDAVANT = 2;
    static final int A_DRETA = 3;
    static final int A_ESQUERRA = 4;

    static final int DURACIO_ESQUIVA = 6;

    Estat estat;
    Random r = new Random();
    int esquiva = 0;
    int accio = 0;
    int repetir = 0;

    public Bitxo27(Agents pare) {
        super(pare, "Nou", "imatges/robotank1.gif");
    }

    @Override
    public void inicia() {
        // atributsAgents(v,w,dv,av,ll,es,hy)
        int cost = atributsAgent(6, 7, 600, 30, 23, 5, 5);
        System.out.println("Cost total:" + cost);

        // Inicialització de variables que utilitzaré al meu comportament
    }

    @Override
    public void avaluaComportament() {
        estat = estatCombat();
        if (repetir != 0) {
            repetir();
            repetir--;
        } else if (esquiva != 0) {
            esquiva();
        } else {
            gestionarComida();
            gestionarPared();
            gestionarColisio();
        }
    }

    private void repetir() {
        atura();
        switch (accio) {
            case A_ENRERE:
                enrere();
                break;
            case A_ENDAVANT:
                endavant();
                break;
            case A_DRETA:
                dreta();
                break;
            case A_ESQUERRA:
                esquerra();
                break;
        }
    }

    private void gestionarColisio() {
        if (estat.enCollisio) {
            atura();
            enrere();
            repetir = 4;
            accio = A_ENRERE;
        }
    }

    private void gestionarPared() {
        if (hiHaParet(20)) {
            if (estat.objecteVisor[CENTRAL] == PARET) {
                if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
                    atura();
                    gira(20 + r.nextInt(20));
                    endavant();
                } else {
                    atura();
                    gira((20 + r.nextInt(20)) * -1);
                    endavant();
                }
            }

        } else if (hiHaParet(25) && Math.abs(estat.distanciaVisors[ESQUERRA] - estat.distanciaVisors[DRETA]) > 50) {
            if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
                System.out.println("Esquiv esquerra");
                //setDireccioEsquiva(A_ESQUERRA);
                repetir = 3;
                accio = A_ESQUERRA;
            } else {
                System.out.println("Esquiv dreta");
                //setDireccioEsquiva(A_DRETA);
                repetir = 3;
                accio = A_DRETA;
            }
        } else if (hiHaParet(85) && estat.distanciaVisors[CENTRAL] < 85) {
            if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
                atura();
                esquerra();
                endavant();
            } else {
                atura();
                dreta();
                endavant();
            }
        } else {
            atura();
            endavant();
        }
    }

    private void gestionarComida() {
        mirar(); //Miramos a la comida o escudo más cercanos
        int aux = comidaEnem();
        switch (aux) {
            case 2: //SI esta en el sector izquierdo esquivamos por la derecha y viceversa
                setDireccioEsquiva(A_DRETA);
                break;
            case 3:
                setDireccioEsquiva(A_ESQUERRA);
                break;
            case 0:
                endavant();
        }
    }

    private void setDireccioEsquiva(int direccio) {
        if (direccio == A_DRETA) {
            esquiva = DURACIO_ESQUIVA;
        } else {
            esquiva = -DURACIO_ESQUIVA;
        }
    }

    private void esquiva() {
        atura();
        if (esquiva > 0) {
            if (esquiva > DURACIO_ESQUIVA / 2) {
                dreta();
            } else {
                esquerra();
            }
            esquiva--;
        } else {
            if (esquiva < -DURACIO_ESQUIVA / 2) {
                esquerra();
            } else {
                dreta();
            }
            esquiva++;
        }
        endavant();
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
