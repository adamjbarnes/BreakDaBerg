package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Level3 implements Screen {
    final MyGdxGame game;

    Texture bergImage;
    Texture boatImage;
    Texture whaleImage;
    Texture buoyImage;
    Sound bergSound;
    Music oceanMusic;
    //SpriteBatch batch;
    OrthographicCamera camera;
    Rectangle boat;
    Array<Rectangle> bergs;
    Array<Rectangle> whales;
    Array<Rectangle> buoys;
    double spawntime;
    long lastbergTime;
    long lastwhaleTime;
    long lastbuoyTime;
    int bergsGathered;


    public Level3(final MyGdxGame game) {
        this.game = game;
        spawntime = 1000000000*0.9;
        // load the images for the droplet and the bucket, 64x64 pixels each
        bergImage = new Texture(Gdx.files.internal("iceberg.png"));
        boatImage = new Texture(Gdx.files.internal("boat.png"));
        whaleImage = new Texture(Gdx.files.internal("whale.png"));
        buoyImage =  new Texture(Gdx.files.internal("buoy.png"));

        // load the drop sound effect and the rain background "music"
        bergSound = Gdx.audio.newSound(Gdx.files.internal("bergcrash.mp3"));
        oceanMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
        oceanMusic.setLooping(true);


        // create the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // create a Rectangle to logically represent the bucket
        boat = new Rectangle();
        boat.x = 800 / 2 - 64 / 2; // center the bucket horizontally
        boat.y = 20; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
        boat.width = 94;
        boat.height = 70;

        bergs = new Array<Rectangle>();
        spawnBerg();

        whales = new Array<Rectangle>();
        spawnWhale();

        buoys = new Array<Rectangle>();
        spawnBuoy();
    }

    private void spawnBerg() {
        Rectangle berg = new Rectangle();
        berg.x = MathUtils.random(0, 800-64);
        berg.y = 480;
        berg.width = 32;
        berg.height = 32;
        bergs.add(berg);
        lastbergTime = TimeUtils.nanoTime();
    }

    private void spawnWhale() {
        Rectangle whale = new Rectangle();
        whale.x = MathUtils.random(0, 800-64);
        whale.y = 480;
        whale.width = 32;
        whale.height = 32;
        whales.add(whale);
        lastwhaleTime = TimeUtils.nanoTime();
    }

    private void spawnBuoy() {
        Rectangle buoy = new Rectangle();
        buoy.x = MathUtils.random(0, 800-64);
        buoy.y = 480;
        buoy.width = 16;
        buoy.height = 16;
        buoys.add(buoy);
        lastbuoyTime = TimeUtils.nanoTime();
    }

    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
        // arguments to clear are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        ScreenUtils.clear(0, 0, 0.2f, 1);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

        // begin a new batch and draw the boat and
        // all bergs
        game.batch.begin();
        game.font.draw(game.batch, "Bergs hit: " + bergsGathered, 0, 480);
        game.batch.draw(boatImage, boat.x, boat.y, boat.width, boat.height);
        for (Rectangle iceberg : bergs) {
            game.batch.draw(bergImage, iceberg.x, iceberg.y);
        }
        for (Rectangle freewilly : whales) {
            game.batch.draw(whaleImage, freewilly.x, freewilly.y);
        }
        for (Rectangle redcan : buoys) {
            game.batch.draw(buoyImage, redcan.x, redcan.y);
        }
        game.batch.end();

        // process user input
        if(Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            boat.x = touchPos.x - 64 / 2;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) boat.x -= 200 * Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) boat.x += 200 * Gdx.graphics.getDeltaTime();

        // make sure the bucket stays within the screen bounds
        if(boat.x < 0) boat.x = 0;
        if(boat.x > 800 - 64) boat.x = 800 - 64;

        // check if we need to create a new iceberg and whale
        if(TimeUtils.nanoTime() - lastbergTime > spawntime) spawnBerg();
        if(TimeUtils.nanoTime() - lastwhaleTime > spawntime) spawnWhale();
        if(TimeUtils.nanoTime() - lastbuoyTime > spawntime) spawnBuoy();


        // a sound effect as well.
        for (Iterator<Rectangle> iter = bergs.iterator(); iter.hasNext(); ) {
            Rectangle bergz = iter.next();
            bergz.y -= 200 * Gdx.graphics.getDeltaTime();
            if(bergz.y + 64 < 0) iter.remove();
            if(bergz.overlaps(boat)) {
                bergsGathered++;
                bergSound.play();
                iter.remove();
            }
        }
        for (Iterator<Rectangle> iter = whales.iterator(); iter.hasNext(); ) {
            Rectangle whales = iter.next();
            whales.y -= 200 * Gdx.graphics.getDeltaTime();
            if(whales.y + 64 < 0) iter.remove();
            if(whales.overlaps(boat)) {
                bergsGathered--;
                bergSound.play();
                iter.remove();
            }
        }
        for (Iterator<Rectangle> iter = buoys.iterator(); iter.hasNext(); ) {
            Rectangle buoys = iter.next();
            buoys.y -= 200 * Gdx.graphics.getDeltaTime();
            if(buoys.y + 64 < 0) iter.remove();
            if(buoys.overlaps(boat)) {
                bergsGathered--;
                bergSound.play();
                iter.remove();
            }
        }

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
        oceanMusic.play();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        // dispose of all the native resources
        bergImage.dispose();
        boatImage.dispose();
        bergSound.dispose();
        oceanMusic.dispose();
    }
}
