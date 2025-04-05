polyfill();

window.addEventListener('shownextword', () => {
    if(isHidden){
    const isCompleted = showNext();
    if (isCompleted) {
        // Signal to Android that we've shown all words and should move to next ayah
        if (window.Android) {
            window.Android.moveToNextAya();
        }
    }}
});

window.addEventListener('undonextword', () => {
    if(isHidden){
    const isFirst = showPrev();
    if (isFirst) {
        // Signal to Android that we've hidden all words and should move to previous ayah
        if (window.Android) {
            window.Android.moveToPrevAya();
            isSwipe = false;
        }
    }}
});

let isHidden = false;

function receiveAya(ayaText) {
    var words = ayaText.split(' ');
    const ayaTextDiv = document.getElementById('ayaText')
    ayaTextDiv.innerHTML = '';
    words.forEach(word => {
        const span = document.createElement('span');
        span.textContent = word;
        if(isHidden) {span.style.opacity = '0'; currentIndex = 0;}
        span.classList.add('word');
        ayaTextDiv.appendChild(span);
        ayaTextDiv.appendChild(document.createTextNode(' '));
    });

    ayaTextDiv.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
}

function receiveAyaPrev(ayaText) {
    var words = ayaText.split(' ');
    const ayaTextDiv = document.getElementById('ayaText')
    ayaTextDiv.innerHTML = '';
    words.forEach(word => {
        const span = document.createElement('span');
        span.textContent = word;
        if(isHidden) {span.style.opacity = '1'; currentIndex = words.length;}
        span.classList.add('word');
        ayaTextDiv.appendChild(span);
        ayaTextDiv.appendChild(document.createTextNode(' '));
    });
    ayaTextDiv.scrollTo({
        top: ayaTextDiv.scrollHeight,
    });
}



function setMaxHeight() {
    const viewportHeight = window.innerHeight;

    const lineHeightPx = 4 * parseFloat(getComputedStyle(document.documentElement).fontSize);

    const numLines = Math.floor(viewportHeight / lineHeightPx);

    const maxHeightRem = numLines * 4;

    const element = document.getElementById('ayaText');

    element.style.maxHeight = `${maxHeightRem}rem`;
}

setMaxHeight();

window.addEventListener('resize', setMaxHeight);

let currentIndex = 0;




function toggleHideShow() {

    currentIndex = 0;
    const wordElements = document.querySelectorAll('.word');
    const ayaTextDiv = document.getElementById('ayaText');

    if (!isHidden) {

        wordElements.forEach(element => {
            element.style.opacity = '0';
        });
        ayaTextDiv.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    } else {

        wordElements.forEach(element => {
            element.style.opacity = '1';
        });
        ayaTextDiv.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }


    isHidden = !isHidden;
}




function showNext() {
    const wordElements = document.querySelectorAll('.word');
    const ayaTextDiv = document.getElementById('ayaText');

    if (currentIndex < wordElements.length) {
        const nextWord = wordElements[currentIndex];
        nextWord.style.opacity = '1';
        currentIndex++;

        const wordRect = nextWord.getBoundingClientRect();
        const ayaRect = ayaTextDiv.getBoundingClientRect();

        const isWordVisible = wordRect.bottom <= ayaRect.bottom && wordRect.top >= ayaRect.top;

        if (!isWordVisible) {
            ayaTextDiv.scrollTo({
                top: ayaTextDiv.scrollTop + wordRect.top - ayaRect.top,
                behavior: 'smooth'
            });
        }
    }

    else {
        if(currentIndex < wordElements.length + 1)
            currentIndex++
    }

    return currentIndex > wordElements.length;
}


function showPrev(ayaNo) {
    const wordElements = document.querySelectorAll('.word');
    const ayaTextDiv = document.getElementById('ayaText');

    if (currentIndex > 0) {
        currentIndex--;
        const prevWord = wordElements[currentIndex];
        prevWord.style.opacity = '0';

        const wordRect = prevWord.getBoundingClientRect();
        const ayaRect = ayaTextDiv.getBoundingClientRect();

        const isWordVisible = wordRect.bottom <= ayaRect.bottom && wordRect.top >= ayaRect.top;

        if (!isWordVisible) {
            const scrollAmount = wordRect.bottom - ayaRect.bottom;
            ayaTextDiv.scrollTo({
                top: ayaTextDiv.scrollTop + scrollAmount,
                behavior: 'smooth'
            });
            if (currentIndex < wordElements.length)
               wordElements[currentIndex++].style.opacity = '1';
        }
    }

    else {
        if(currentIndex > -1 && ayaNo !== 1)
            currentIndex--
    }

    if(ayaNo !== 1)
        return currentIndex === -1;
    else
        return currentIndex === 0;
}


let startX = 0;
let startY = 0;
const swipeThreshold = 24;
let interval;
let isSwipe = false;
let hasMoved = false;
let previousX = 0;
let previousY = 0;
let movementDirection = null;


function handleHorizontalSwipe(event) {
    let currentX = event.touches[0].clientX;
    let currentY = event.touches[0].clientY;

    // Calculate deltas from start position
    let deltaX = Math.abs(currentX - startX);
    let deltaY = Math.abs(currentY - startY);

    // Calculate deltas from previous position to detect direction
    let deltaXFromPrev = currentX - previousX;
    let deltaYFromPrev = currentY - previousY;

    // Detect movement direction early
    if (Math.abs(deltaXFromPrev) > Math.abs(deltaYFromPrev) && movementDirection === null) {
        movementDirection = "horizontal";
        // Early swipe detection
        isSwipe = true;
    } else if (Math.abs(deltaYFromPrev) > Math.abs(deltaXFromPrev) && movementDirection === null) {
        movementDirection = "vertical";
    }

    // For any significant movement
    if (deltaX > swipeThreshold/2 || deltaY > swipeThreshold/2) {
        hasMoved = true;
    }

    // For horizontal swipes in landscape mode
    // Only trigger if the movement is primarily horizontal (diagonal check)
    if (movementDirection === "horizontal" && Math.abs(deltaX) > swipeThreshold &&
        // Add this condition to filter out diagonal swipes
        deltaY < deltaX * 0.5 && !interval) {

        // FIXED: Trigger undonextword when swiping RIGHT (positive X delta)
        let directionEvent = (currentX - startX) > 0 ? new Event('undonextword') : new Event('undonextword');
        if (directionEvent) {
            // Fire the event immediately first
            window.dispatchEvent(directionEvent);

            // Then set up the interval for subsequent triggers
            interval = setInterval(() => {
                window.dispatchEvent(directionEvent);
            }, 400);
        }
    }

    // Update previous positions
    previousX = currentX;
    previousY = currentY;
}


document.addEventListener("touchstart", (event) => {
    startX = event.touches[0].clientX;
    startY = event.touches[0].clientY;
    previousX = startX;
    previousY = startY;
    isSwipe = false;
    hasMoved = false;
    movementDirection = null;
});

document.addEventListener("touchmove", handleHorizontalSwipe);

document.addEventListener("touchend", () => {
    clearInterval(interval);
    interval = null;

    // Only dispatch shownextword if there was NO movement at all
    if (!isSwipe && !hasMoved) {

    }
    isSwipe = false;
    hasMoved = false;
    movementDirection = null;
});

document.addEventListener("click", () => {
const event = new Event('shownextword');
        window.dispatchEvent(event);
        });
