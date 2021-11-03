package agents;

// Exemple de Bitxo
import java.util.Random;

public class Bitxo26 extends Agent {

    static final int PARET = 0;
    static final int BITXO = 1;
    static final int RES = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    static final int distMaxBales = 400;
    Estat estat;
    Random r = new Random();
    int comptadorGir = 120;
    int noMirar = 0;
    int comptadorColisio = 0;
    int recursosAnterior = 0;

    public Bitxo26(Agents pare) {
        super(pare, "Exemple26", "imatges/robotank1.gif");
    }

    @Override
    public void inicia() {
        // atributsAgents(v,w,dv,av,ll,es,hy)
        int cost = atributsAgent(6, 5, 600, 45, 23, 5, 3);
        System.out.println("Cost total:" + cost);

        // Inicialització de variables que utilitzaré al meu comportament
    }

    @Override
    public void avaluaComportament() {
        estat = estatCombat();
        atura();
        if (noMirar == 0) {
            miraRecurs();
            evaluarDisparo();
        } else {
            noMirar--;
        }
        camina();
    }

    private void camina() {
        //Estamos en colisión
        if (estat.enCollisio) {
            atura();
            comptadorColisio++;
            gestionaColisio();
        } else {
            comptadorColisio = 0;
            //Hay una pared cerca
            if (hiHaParet(30)) {
                giraAProp();

            } else if (hiHaParet(85)) {    //Hay una pared relativamente lejos
                giraLluny();

            } else if (comptadorGir <= 0) {
                girRecon();
            } else {
                System.out.println("endavant");
                endavant();
            }
            gestionarEnemic();
        }
    }

    private void gestionarEnemic() {
        if (estat.llançamentEnemicDetectat && !estat.escutActivat) {
            if (estat.distanciaLlançamentEnemic < 30) {
                activaEscut();
                noMirar = 5;
            }
            endavant();
        }
        if(recursosAnterior > estat.recursosAgafats){
            if(!estat.escutActivat){
                activaEscut();
            }
        }
        recursosAnterior = estat.recursosAgafats;   
        
    }

    private void gestionaColisio() {
        if (comptadorColisio > 50) {
            hyperespai();
        } else {
            boolean a = r.nextBoolean();
            if (a) {
                esquerra();
            } else {
                dreta();
            }

            a = r.nextBoolean();
            if (a) {
                endavant();
            } else {
                enrere();

            }
            //noMirar = 5;
        }
    }

    private void miraRecurs() { //Falta evaluar los escudos
        Objecte fin = null;
        int distMin = 99999999;
        if (estat.veigAlgunRecurs) {
            for (int i = 0; i < estat.numObjectes; i++) { //Recorrem tots els recursos
                Objecte aux = estat.objectes[i];
                if (aux.agafaTipus() == 100 + estat.id) {
                    if (distMin > aux.agafaDistancia()) {
                        distMin = aux.agafaDistancia();
                        fin = aux;
                    }
                }
            }
        }
        if ((estat.veigAlgunEscut && estat.escuts < 5) || !estat.veigAlgunRecurs) {
            for (int i = 0; i < estat.numObjectes; i++) { //Recorrem tots els recursos
                Objecte aux = estat.objectes[i];
                if (aux.agafaTipus() == Estat.ESCUT) {
                    if (distMin > aux.agafaDistancia()) {
                        distMin = aux.agafaDistancia();
                        fin = aux;
                    }
                }
            }
        }

        if (fin != null) {
            mira(fin);
        } else {
            comptadorGir--;
        }
    }

    private void evaluarDisparo() {
        Objecte fin = null;
        int distMin = 9999999;
        if (!estat.llançant && (estat.veigAlgunRecurs || estat.veigAlgunEnemic)) {
            for (int i = 0; i < estat.numObjectes; i++) { //Recorrem tots els objectes
                Objecte aux = estat.objectes[i];
                if (((aux.agafaTipus() >= 100 && aux.agafaTipus() != (100 + estat.id)) || aux.agafaTipus() == Estat.AGENT) && (aux.agafaSector() == 2 || aux.agafaSector() == 3) && aux.agafaDistancia() < distMaxBales) {
                    if (aux.agafaTipus() == Estat.AGENT && aux.agafaDistancia() <= 100) {
                        fin = aux;
                        break;
                    } else if (distMin > aux.agafaDistancia()) {
                        distMin = aux.agafaDistancia();
                        fin = aux;
                    }
                }
            }
        }
        if (fin != null && estat.llançaments > 0 && !estat.llançant) {
            if (fin.agafaTipus() == Estat.AGENT && fin.agafaDistancia() <= 100) {
                mira(fin);

                llança();

            } else if (fin.agafaTipus() != Estat.AGENT) {
                mira(fin);

                llança();

            }

        }
    }

    private void giraAProp() {
        if (estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]) {
            atura();
            gira(20 + r.nextInt(20));
            endavant();

        } else {
            atura();
            gira(-1 * (20 + r.nextInt(20)));
            endavant();
        }
    }

    private void giraLluny() {
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

    private void girRecon() {
        atura();
        if (r.nextBoolean()) {
            gira(120);
        } else {
            gira(-120);
        }
        endavant();
        comptadorGir = 120;
    }

    private boolean hiHaParet(int distancia) {
        return (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < distancia)
                || (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < distancia)
                || (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < distancia);
    }
}
