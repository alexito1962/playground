public class NetworkedHome {
    public static void main(String[] args){
      // Adapterparameter
		short[] myPort = {80,80,81,88,88,88};
		String[] myIp = {"192.168.7.50","192.168.7.51","192.168.7.51","192.168.7.52","192.168.7.52","192.168.7.52"};
		String[] myEndpoint = {"light/0","light/1","light/1","light/0","light/1","light/2"};
		Switch[] mySwitch = new Switch[8];
        Light[] addLight = new Light[5];


        // Testen Sie Ihr Programm, indem Sie verschiedene Schalter-Instanzen anlegen und mit Lichtern verknüpfen. 
        // Jede Schalter-Instanz sollte im App-Fenster unten angezeigt werden. Solange Ihr Programm läuft, können Sie die angezeigten Schalter verwenden, 
        // um damit verbundenen Lichter an- oder auszuschalten.

        Light oneLight = new Light(myIp[0],myPort[0],myEndpoint[0]);

		for (int i = 0; i <= 8; i++) {
	    mySwitch[i] = new Switch(oneLight);
        mySwitch[i].turnOff();
			for (int j = 1; j <= 5; j++) {
    			addLight[j] = new Light(myIp[j],myPort[j],myEndpoint[j]);
                addLight[j].setHue ((int)(Math.random()*360));
                addLight[j].setBrightness(Math.random());
                addLight[j].setSaturation(Math.random());                
                
           	 	if (mySwitch[i].linkLight(addLight[j]))  {
                    System.out.println("For Switch " + i + " Light " + j + " is added");
                } else {
                    System.out.println("For Switch " + i + " Light " + j + " cannot be added");
                    break;
                }
			} // for .. j
        } // for i..
    }
}

// Klasse Light
class Light {
    // (die Sichtbarkeit sollte so restriktiv wie möglich sein):
    // Einen Wahrheitwert mit der Bezeichnung isOn, der speichert ob das Licht gerade an, oder aus ist
    // Ein 32-Bit Ganzzahl hue, welche den aktuell eingestellten Farbwinkel speichert.
    // Zwei double-Werte brightness und saturation, welche die Helligkeit und Sättigung des Lichts aufnehmen werden.
    private boolean isOn; 
	private int hue;
    private double brightness;
    private double saturation;
    private final Adapter adapter;

    // Öffentlichen Parameterkonstruktor für das Licht der  .. IP, Port, Endpoint .. des Lichts annimmt
    // Der Konstruktor soll mit diesen Parameterwerten eine Instanz der Klasse Adapter erzeugen...
    // und diese in einem konstanten Instanzattribut mit der Bezeichnung adapter ablegen.
    
    public Light(String ip, short port, String endpoint) {
		this.adapter = new Adapter(ip, port, endpoint);
        this.turnOff();
        this.setHue(270);
        this.setBrightness(0.8);
        this.setSaturation(1.0); 
    }
    // Schreiben Sie öffentliche Instanzmethoden isOn, getHue, getBrightness und getSaturation, 
    // mit denen Sie die zugehörigen Attributswerte auslesen können.
    public boolean isOn() {
        return isOn;
    }
    
    public int getHue() {
		return hue;
    }
    
    public double getBrightness() {
		return brightness;
    }
    
    public double getSaturation() {
		return saturation;
    }
    
    // Schreiben Sie eine öffentliche Instanzmethode setHue, mit der der Farbwinkel eingestellt werden kann. 
    // der gespeicherte Winkel immer im Wertebereich [0° ... 360°[ liegt (d.h. statt 370°, sollen 10° gespeichert werden, für -10° sollen 350° gespeichert werden)
    // Der Wert muss nicht nur im zugehörigen Attribut gespeichert, sondern auch mit dem Kommando "hue", an den Adapter gesendet werden. 
    // Die Methode soll (wie alle folgenden set-Methoden) kein Ergebnis zurückgeben.
    
    public void setHue(int newHue) {
        this.hue = newHue;
        this.hue = newHue % 360;
        if (this.hue < 0) { this.hue = this.hue + 360; }
		this.adapter.send("hue",this.hue);
    } // setHue
    
    // Schreiben Sie eine weitere öffentliche Instanzmethode setBrightness. Der Wertebereich der Helligkeit ist [0.01 ... 1.0]
    // (d.h. statt 1.5 soll der Wert 1.0, statt -1 der Wert 0.01 gespeichert und gesendet werden)
    // Der Wert muss nicht nur im zugehörigen Attribut gespeichert, sondern auch mit dem Kommando "brightness" an den Adapter gesendet werden.
    
    public void setBrightness(double newBrightness) {
        this.brightness = newBrightness;
        if (this.brightness > 1.0) { this.brightness = 1.0; }
        if (this.brightness <= 0) { this.brightness = 0.01; }
		this.adapter.send("brightness",this.brightness);
    } // setBrightness
    
    // Schreiben Sie nun analog zu 3c) eine öffentliche Instanzmethode setSaturation. Der Wertebereich der Sättigung ist [0.0 ... 1.0]. 
    public void setSaturation(double newSaturation) {
        this.saturation = newSaturation;
        if (this.saturation > 1.0) { this.saturation = 1.0; }
        if (this.saturation <= 0) { this.saturation = 0.00; }       
 
		this.adapter.send("saturation",this.saturation);
    } // setSaturation
    
    // Fügen Sie zuletzt noch die öffentlichen Instanzmethoden turnOn und turnOff ein
    // Diese sollen zum einen den aktuellen Schaltzustand in dem zugehörigen Attribut ablegen und außerdem entweder das "on" oder "off"-Kommando an den Adapter senden.
    
    public void turnOn() {
        this.isOn = true;
        this.adapter.send("on");
    } //turnOn
    
    public void turnOff() {
        this.isOn = false;
        this.adapter.send("off");
    } //turnOff
   
} // Klasse Light 

// Klasse Switch

// Schreiben Sie eine nicht öffentliche Klasse Switch, welche die folgenden Attribute enthält (die Sichtbarkeit soll dabei so restriktiv wie möglich sein):
// Einen Wahrheitswert mit der Bezeichnung isOn, der speichert, ob der Schalter gerade an oder aus ist.
// Ein konstantes Attribut lights, das 5 Lichter speichern kann.
// Ein konstantes Attribut adapter, das eine SimpleAdapter-Instanz speichern kann.

class Switch {

    private boolean isOn; 
    private final Light[] lights = new Light[5];
    private final SimpleAdapter adapter;
    private static int ID = 1;
    
    // Schreiben Sie einen öffentlichen Parameterkonstruktor, der ein mit dem Schalter verbundenes Licht als Parameter annimmt. 
    // Das übergebene Licht soll als eines der 5 verbundenen Lichter gespeichert werden.
    // Des Weiteren soll im Konstruktor eine SimpleAdapter-Instanz erzeugt werden. Mit dessen Hilfe können Sie den angezeigten Schaltzustand des Schalters in Ihrer App festlegen. 
    // Die dafür benötigte ID muss für jeden Schalter eindeutig sein. Verwenden Sie dafür eine fortlaufende Nummerierung beginnend bei 1 
    // (also der erste Schalter der erzeugt wurde verwendet ID=1, der zweite 2 usw.).
    // Registrieren Sie im Konstruktor jede Schalterinstanz (mit Hilfe der Klasse Home), 
    // damit ein Schalter in Ihrer App angezeigt wird. Verwenden Sie bei der Registrierung dieselbe ID, die Sie auch dem SimpleAdapter übergeben haben
    
    public Switch(Light oneLight) {
       this.lights[0] = oneLight;
       this.adapter = new SimpleAdapter(ID);
        
       // Ergänzen Sie Ihren Konstruktor so, dass Schalter im aus-Zustand initialisiert werden und diesen Zustand auch an die App übertragen wird. 
       // Stellen Sie auch sicher, dass das übergebene Licht denselben Zustand wie der Schalter hat. Nutzen Sie dafür bereits existierende Methoden Ihrer Klassen.
       this.turnOff();
       this.lights[0].turnOff();
        
       Home.registerSwitch(ID, this);
       ID = ID + 1;
    } // Switch Constructor
    
    // Schreiben Sie eine öffentliche Instanzmethode isOn, mit der Sie das zugehörige Attribut auslesen können.
    public boolean isOn() {
        return this.isOn;
    } // isOn 

    // Ergänzen Sie die öffentlichen Instanzmethoden turnOn und turnOff. Diese sollen zum einen den aktuellen Schaltzustand in dem zugehörigen Attribut ablegen 
    // und zum anderen entweder das "on" oder "off"-Kommando an den Adapter senden. Zudem muss auch der Zustand aller mit dem Schalter verbundenen Lichter entsprechend geändert werden.
    
    public void turnOn() {
        this.isOn = true;
    	this.adapter.send("on");
        this.lights[0].turnOn();
        for (int i = 1 ; i <= 4; i++) {
            if(this.lights[i] != null) {
                this.lights[i].turnOn();
            } else {
                break;
            }
        } // for ...i 
    } // turnOn

    public void turnOff() {
        this.isOn = false;
     	this.adapter.send("off");
        this.lights[0].turnOff();
        for (int i = 1 ; i <= 4; i++) {
            if(this.lights[i] != null) {
                this.lights[i].turnOff();
            } else {
                break;
            }
        } // for .. i
    } // turnOff
    
    // Schreiben Sie eine öffentliche Instanzmethode linkLight, mit der ein übergebenes Licht verbunden werden kann. 
    // Falls der Schalter das neue Licht verwalten kann, soll die Methode true zurückgeben. Falls die 5 Plätze für verbundene Lichter bereits belegt sind muss false zurückgegeben werden.
    
    public boolean linkLight(Light oneLight) {
        for (int i = 1 ; i <= 4; i++) {
            if(this.lights[i] == null) {
                this.lights[i] = oneLight;
				this.lights[i].turnOff();
                if (this.isOn()) { 
                    this.lights[i].turnOn(); 
                }
                return true;
            } //if null
        } //for 
        return false;
    } // linkLight
} // Klasse Switch
