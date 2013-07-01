import main.scala.TreeQuencer.{NodeMaterial, app};
import org.mt4j.util.math.Tools3D;

new NodeMaterial(Tools3D.getGL(app)) {
  setAmbient(Array( 0.33f, 0.39f, 0.43f, 0.95f ));
  setDiffuse(Array( 0.33f, 0.39f, 0.43f, 0.95f ));
  setSpecular(Array( 2*0.33f, 2*0.39f, 2*0.43f, 0.95f ))
  setEmission(Array( 0.1f, 0.1f, 0.1f, 0.0f ));;
  setShininess(80);
}