polyfill();

window.addEventListener('contextmenu', (event) => {
    event.preventDefault();
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

    return currentIndex > wordElements.length
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
        return currentIndex === -1
    else
        return currentIndex === 0
}





