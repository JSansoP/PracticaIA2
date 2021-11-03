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
    
    //Declaración de variables
    
    static final int distMaxBales = 400;
    Estat estat;
    Random r = new Random();
    int comptadorGir = 120;
    int noMirar = 0;
    int comptadorColisio = 0;
    int recursosAnterior = estat.recursosAgafats;

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
    
    /**
     * El método camina se encarga del desplazamiento de nuestro agente a través
     * del entorno, comprobando en primer lugar si estamos en colisión sino 
     * en funcion de la distancia que estemos a las paredes nuetro agente girará
     * más o menos, luego si pasa un tiempo en que el agente no ve ningun recuros
     * entonces hace un giro de 120º aleatorio para volver a evaluar el entorno 
     * y por último en cada iteración gestionamos las acciones que realiza el 
     * agente sobre un agente enemigo
     */
    
    private void camina() {
        //Estamos en colisión
        if (estat.enCollisio) {
            atura();
            comptadorColisio++;
            gestionaColisio();
        } else {
            comptadorColisio = 0;
            //Hay una pared cerca
            if (hiHaParet(19)) {
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
    
    /**
     * Este método se encarga de: 
     * En primer lugar, detectar si uno de nuestros sectores observa un disparo 
     * enemigo y si no tenemos el escudo activado una vez se haya cumplido dicha
     * condición verificamos si el lanzamiento se encunetra cerca de nosotro si 
     * es asi activamos el escudo.
     * En segundo lugar, detectamos si el enemigo nos ha impacatado una disparo,
     * simplemente mirando el numero de recuros que tenemos, si es asi activamos 
     * el escudo.
     * 
     */
    
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
    
    /**
     * Este método se encarga de gestionar la acciones que realiza el agente
     * en colisión, primero, llevamos a cabo la verificación de un contador 
     * de colisión, es decir, si nuestro agente lleva un bastante tiempo 
     * colisionando activamos el hiperespacio, sino intentamos evitar la colisión,
     * realizando un giro y un movimiento aleatorio.
     */
    
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
    
    /**
     * Este método se encarga de:
     * Buscar nuestros recursos dentro del entorno de manera prioritaria, pero 
     * si vemos algun escudo y tenemos pocos o si no vemos ningun recurso cerca
     * vamos a por los escudos.
     * Mientras no veamos nada durante un tiempo vamos decrementando el contador 
     * de gira para asi realizar un giro y volver a evaluar el entorno.
     */
    
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
    
    /**
     * En este método gestionamos las distintas acciones en las cuales disparamos.
     * En primer lugar, mientras no hemos lanzado ninguna bala y vemos un recurso o 
     * algun enemigo, recorremos todos los objetos, para luego posteriormente 
     * evaluear si es un recurso enemigo y lo vemos en algunos de los secotres 
     * y la distancia a la que se encuntra es relativamnete cerca entonces, verificamos
     * si el enemigo esta cerca entonces disparamos al enemigo, pero si no vemos
     * ningun enemigo, quiere decir que vemos un recurso enemigo, también le disparamos.
     */
    
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
    
    /**
     * Método que se encarga de girar unos grados entre [20º - 40º] en función
     * a la distancia que se encuntran los visores de la izquierda o derecha.
     * Si el visor de la izquierda es mayor a la derecha giramos unos grados
     * aleatorios dentro del rango hacia la izquierda en caso contrario giramos
     * un número aleatorio dentro del rango hacia la derecha.
     */
    
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
    
    /**
     * Método que se encarga de realizar un giro de 120º a la izquierda o derecha
     * de forma aleatoria 
     */
    
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
    
    /**
     * Método que se encarga de devolver si nuestro agente ve una paret a una 
     * distáncia que recibe por parámetro.
     * @param distancia
     * @return 
     */
    
    private boolean hiHaParet(int distancia) {
        return (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < distancia)
                || (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < distancia)
                || (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < distancia);
    }
}
