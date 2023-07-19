import processing.sound.*;

Amplitude amp;
AudioIn in;

float ampSize;

float inc = 0.03;
int scl = 10;
float zoff = 0;

int cols;
int rows;

int noOfPoints = 6000;

int counter = 0;

Particle[] particles = new Particle[noOfPoints];
PVector[] flowField;

void setup() {
  fullScreen(P2D);
  orientation(LANDSCAPE);

  background(255);
  hint(DISABLE_DEPTH_MASK);

  cols = floor(width/scl);
  rows = floor(height/scl);

  flowField = new PVector[(cols*rows)];

  for (int i = 0; i < noOfPoints; i++) {
    particles[i] = new Particle();
  }
  
  amp = new Amplitude(this);
  in = new AudioIn(this, 0);
  in.start();
  amp.input(in);
}

void draw() {
  counter++;
  if (counter > 6001){
    counter = 0;
  }
  
  ampSize = map(amp.analyze(), 0, 1, 1.5, 5);
  
  fill(255, 5);
  rect(0, 0, width, height);
  noFill();

  float yoff = 0;
  for (int y = 0; y < rows; y++) {
    float xoff = 0;
    for (int x = 0; x < cols; x++) {
      int index = (x + y * cols);

      float angle = noise(xoff, yoff, zoff) * TWO_PI;
      PVector v = PVector.fromAngle(angle);
      v.setMag(0.01);

      flowField[index] = v;

      stroke(0, 100);

      //pushMatrix();

      //translate(x*scl, y*scl);
      //rotate(v.heading());
      //line(0, 0, scl, 0);

      //popMatrix();

      xoff = xoff + inc;
    }
    yoff = yoff + inc;
  }
  zoff = zoff + (inc / 50);

  for (int i = 0; i < particles.length; i++) {
    particles[i].follow(flowField);
    particles[i].update();
    particles[i].edges();
    particles[i].show();
  }
  
  println(counter);
}

class Particle {
  PVector pos = new PVector(random(width), random(height));
  PVector vel = new PVector(0, 0);
  PVector acc = new PVector(0, 0);
  float maxSpeed = 2;

  PVector prevPos = pos.copy();

  public void update() {
    vel.add(acc);
    vel.limit(maxSpeed);
    pos.add(vel);
    acc.mult(0);
  }

  public void follow(PVector[] vectors) {
    int x = floor(pos.x / scl);
    int y = floor(pos.y / scl);
    

    
      int index = (x-1) + ((y-1) * cols);
      // Sometimes the index ends up out of range, typically by a value under 100.
      // I have no idea why this happens, but I have to do some stupid if-checking
      // to make sure the sketch doesn't crash when it inevitably happens.
      //
      index = index - 1;
      if (index > vectors.length || index < 0) {
        //println("Out of bounds!");
        //println(index);
        //println(vectors.length);
        index = vectors.length - 1;
      }
      PVector force = vectors[index];
      applyForce(force);
    }

    void applyForce(PVector force) {
      if (counter <= 3000){
        acc.add(force);
      }
      if (counter >= 3001){
        acc.sub(force);
      }
    }

    public void show() {
      stroke(0, 100);
      strokeWeight(ampSize);
      point(pos.x, pos.y);
    }

    public void updatePrev() {
      prevPos.x = pos.x;
      prevPos.y = pos.y;
    }

    public void edges() {
      if (pos.x > width) {
        pos.x = 0;
        updatePrev();
      }
      if (pos.x < 0) {
        pos.x = width;
        updatePrev();
      }

      if (pos.y > height) {
        pos.y = 0;
        updatePrev();
      }
      if (pos.y < 0) {
        pos.y = height;
        updatePrev();
      }
    }
  }