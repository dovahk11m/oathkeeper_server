const tags = ["카페","공원","맛집","전시관","데이트","쇼핑","술집","책방"];
const places = [
  {id: 1, name: "스타벅스 강남점", tags: ["카페"]},
  {id: 2, name: "서울숲", tags: ["공원"]},
  {id: 3, name: "홍대 맛집", tags: ["맛집","술집"]},
];

const container = document.getElementById("places-container");
const modal = document.getElementById("tagModal");
const modalTags = document.getElementById("modal-tags");
let selectedPlaces = [];

// 랜더링
function renderPlaces() {
  container.innerHTML = "";
  places.forEach(place => {
    const card = document.createElement("div");
    card.className = "place-card";

    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.onchange = (e) => {
      if(e.target.checked) selectedPlaces.push(place.id);
      else selectedPlaces = selectedPlaces.filter(id => id !== place.id);
    };

    const title = document.createElement("div");
    title.className = "place-title";
    title.textContent = place.name;

    const tagContainer = document.createElement("div");
    tagContainer.className = "tags";

    place.tags.forEach(tag => {
      const tagEl = document.createElement("div");
      tagEl.className = "tag";
      tagEl.textContent = tag;

      const removeSpan = document.createElement("span");
      removeSpan.textContent = "✖";
      removeSpan.onclick = () => removeTag(place.id, tag);
      tagEl.appendChild(removeSpan);

      tagContainer.appendChild(tagEl);
    });

    card.appendChild(checkbox);
    card.appendChild(title);
    card.appendChild(tagContainer);

    container.appendChild(card);
  });
}

function removeTag(placeId, tagName) {
  const place = places.find(p => p.id === placeId);
  place.tags = place.tags.filter(t => t !== tagName);
  renderPlaces();
}

// 모달 열기
document.getElementById("bulkAddBtn").onclick = () => {
  if(selectedPlaces.length === 0){
    alert("태그를 추가할 장소를 선택해주세요.");
    return;
  }
  modalTags.innerHTML = "";
  tags.forEach(tag => {
    const label = document.createElement("label");
    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.value = tag;
    label.appendChild(checkbox);
    label.appendChild(document.createTextNode(" " + tag));
    modalTags.appendChild(label);
  });
  modal.style.display = "block";
}

// 모달 저장
document.getElementById("saveTags").onclick = () => {
  const checkedTags = Array.from(modalTags.querySelectorAll("input:checked")).map(cb => cb.value);
  selectedPlaces.forEach(id => {
    const place = places.find(p => p.id === id);
    checkedTags.forEach(tag => {
      if(!place.tags.includes(tag)) place.tags.push(tag);
    });
  });
  modal.style.display = "none";
  renderPlaces();
}

// 모달 닫기 (배경 클릭)
window.onclick = (event) => {
  if (event.target === modal) modal.style.display = "none";
}

renderPlaces();